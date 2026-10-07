package pt.andrerodrigues.faturacao.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.andrerodrigues.faturacao.category.Category;
import pt.andrerodrigues.faturacao.category.CategoryRepository;
import pt.andrerodrigues.faturacao.client.Client;
import pt.andrerodrigues.faturacao.client.ClientRepository;
import pt.andrerodrigues.faturacao.config.DemoProperties;
import pt.andrerodrigues.faturacao.expense.ExpenseRepository;
import pt.andrerodrigues.faturacao.expense.domain.Expense;
import pt.andrerodrigues.faturacao.expense.domain.PaymentMethod;
import pt.andrerodrigues.faturacao.invoice.InvoiceRepository;
import pt.andrerodrigues.faturacao.invoice.domain.Invoice;
import pt.andrerodrigues.faturacao.invoice.numbering.InvoiceNumberGenerator;
import pt.andrerodrigues.faturacao.product.Product;
import pt.andrerodrigues.faturacao.product.ProductRepository;
import pt.andrerodrigues.faturacao.product.ProductType;
import pt.andrerodrigues.faturacao.product.VatRate;
import pt.andrerodrigues.faturacao.user.UserRepository;
import pt.andrerodrigues.faturacao.user.domain.Role;
import pt.andrerodrigues.faturacao.user.domain.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * ARRANQUE (só com o perfil "demo") - cria a conta de demonstração e, se ainda não houver faturas,
 * uma empresa fictícia com 12 meses de atividade: clientes, produtos, faturas em vários estados e despesas.
 * Os dados passam pelas regras reais do domínio (totais, IVA, numeração, fotografias).
 *
 * Fala com:     repositories de todas as áreas, InvoiceNumberGenerator, PasswordEncoder, DemoProperties
 * É usado por:  o Spring, automaticamente, no fim do arranque (depois do AdminInitializer)
 */
@Component
@Profile("demo")
@Order(2)
public class DemoDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private static final ZoneId LISBON = ZoneId.of("Europe/Lisbon");
    private static final String SERIES = "FT";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DemoProperties demoProperties;
    private final CategoryRepository categoryRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final ExpenseRepository expenseRepository;
    private final InvoiceNumberGenerator numberGenerator;

    public DemoDataInitializer(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               DemoProperties demoProperties,
                               CategoryRepository categoryRepository,
                               ClientRepository clientRepository,
                               ProductRepository productRepository,
                               InvoiceRepository invoiceRepository,
                               ExpenseRepository expenseRepository,
                               InvoiceNumberGenerator numberGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoProperties = demoProperties;
        this.categoryRepository = categoryRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.invoiceRepository = invoiceRepository;
        this.expenseRepository = expenseRepository;
        this.numberGenerator = numberGenerator;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createDemoUser();

        if (invoiceRepository.count() > 0) {
            log.info("A base de dados já tem faturas: os dados de demonstração não são criados.");
            return;
        }

        LocalDate today = LocalDate.now(LISBON);
        Random random = new Random(42);

        List<Client> clients = createClients();
        List<Product> products = createProducts();
        Map<String, Category> categories = createCategories();

        int invoices = createInvoices(today, random, clients, products);
        int expenses = createExpenses(today, random, categories);

        log.info("Dados de demonstração criados: {} faturas e {} despesas.", invoices, expenses);
    }

    // ---------- Conta de demonstração ----------

    private void createDemoUser() {
        String email = demoProperties.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        userRepository.save(new User(email, "Conta de demonstração",
                passwordEncoder.encode(demoProperties.password()), Role.USER));
        log.info("Conta de demonstração criada: {}", email);
    }

    // ---------- Catálogo ----------

    private List<Client> createClients() {
        List<Client> existing = clientRepository.findAll();
        List<Client> clients = List.of(
                new Client("Atlântico Digital, Lda.", "505260182", "geral@atlanticodigital.pt", "223 456 789",
                        "Rua de Santa Catarina, 120", "4000-447", "Porto"),
                new Client("Minho Têxteis, S.A.", "511590830", "compras@minhotexteis.pt", "252 310 400",
                        "Avenida 25 de Abril, 45", "4760-101", "Vila Nova de Famalicão"),
                new Client("Clínica Sorriso Braga", "510166130", "admin@clinicasorriso.pt", "253 220 118",
                        "Rua do Souto, 18", "4700-329", "Braga"),
                new Client("Castro & Filhos Construções, Lda.", "501860916", "obras@castrofilhos.pt", null,
                        "Zona Industrial, Lote 7", "4750-128", "Barcelos"),
                new Client("Padaria Flor do Campo", "513909966", "encomendas@flordocampo.pt", null,
                        "Largo da Feira, 3", "4800-443", "Guimarães"),
                new Client("Escola de Línguas Babel", "500308241", "secretaria@babel-linguas.pt", "253 615 902",
                        "Rua Central, 77", "4710-229", "Braga"),
                new Client("Vinhos da Encosta, Lda.", "506281949", "info@vinhosdaencosta.pt", null,
                        "Quinta da Encosta", "4600-012", "Amarante"),
                new Client("Startup Lab Porto", "517865793", "hello@startuplab.pt", null,
                        "Rua do Heroísmo, 300", "4300-259", "Porto"),
                new Client("Ana Martins", "282199357", "ana.martins@email.pt", "912 345 678",
                        "Rua das Flores, 9", "4050-262", "Porto"),
                new Client("Rui Ferreira", "218190930", "rui.ferreira@email.pt", null, null, null, "Lisboa")
        );

        return clients.stream()
                .map(client -> existing.stream()
                        .filter(e -> e.getNif().equals(client.getNif()))
                        .findFirst()
                        .orElseGet(() -> clientRepository.save(client)))
                .toList();
    }

    private List<Product> createProducts() {
        List<Product> existing = productRepository.findAll();
        List<Product> products = List.of(
                new Product("CONS-01", "Consultoria técnica", "Hora de consultoria", ProductType.SERVICE,
                        new BigDecimal("55.00"), VatRate.NORMAL),
                new Product("DEV-01", "Desenvolvimento web", "Hora de desenvolvimento", ProductType.SERVICE,
                        new BigDecimal("40.00"), VatRate.NORMAL),
                new Product("DES-01", "Design UI/UX", "Hora de design", ProductType.SERVICE,
                        new BigDecimal("45.00"), VatRate.NORMAL),
                new Product("MAN-01", "Manutenção mensal de website", null, ProductType.SERVICE,
                        new BigDecimal("120.00"), VatRate.NORMAL),
                new Product("HOST-01", "Alojamento web anual", null, ProductType.SERVICE,
                        new BigDecimal("180.00"), VatRate.NORMAL),
                new Product("FORM-01", "Formação de equipas", "Dia de formação", ProductType.SERVICE,
                        new BigDecimal("650.00"), VatRate.NORMAL),
                new Product("LIV-01", "Manual técnico", "Livro de apoio à formação", ProductType.PRODUCT,
                        new BigDecimal("29.90"), VatRate.REDUCED),
                new Product("CAT-01", "Catering para workshop", "Por pessoa", ProductType.SERVICE,
                        new BigDecimal("14.50"), VatRate.INTERMEDIATE)
        );

        return products.stream()
                .map(product -> existing.stream()
                        .filter(e -> e.getCode().equals(product.getCode()))
                        .findFirst()
                        .orElseGet(() -> productRepository.save(product)))
                .toList();
    }

    private Map<String, Category> createCategories() {
        List<Category> existing = categoryRepository.findAll();
        List<String> names = List.of("Renda", "Software e subscrições", "Telecomunicações", "Combustível",
                "Refeições", "Deslocações", "Material de escritório", "Formação");

        Map<String, Category> categories = new LinkedHashMap<>();
        for (String name : names) {
            Category category = existing.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseGet(() -> categoryRepository.save(new Category(name, null)));
            categories.put(name, category);
        }
        return categories;
    }

    // ---------- Faturas ----------

    private int createInvoices(LocalDate today, Random random, List<Client> clients, List<Product> products) {
        List<LocalDate> issueDates = new ArrayList<>();
        YearMonth month = YearMonth.from(today).minusMonths(11);
        int monthIndex = 0;

        while (!month.isAfter(YearMonth.from(today))) {
            int count = 4 + random.nextInt(3) + monthIndex / 3; // a empresa cresce ao longo do ano
            int lastDay = month.equals(YearMonth.from(today)) ? today.getDayOfMonth() : month.lengthOfMonth();
            for (int i = 0; i < count; i++) {
                issueDates.add(month.atDay(1 + random.nextInt(lastDay)));
            }
            month = month.plusMonths(1);
            monthIndex++;
        }

        // A numeração tem de seguir a ordem das datas: FT 0001 é a mais antiga
        Collections.sort(issueDates);

        boolean cancelledOne = false;
        for (LocalDate issueDate : issueDates) {
            Invoice invoice = new Invoice(clients.get(random.nextInt(clients.size())), issueDate.plusDays(30), null);

            int lines = 1 + random.nextInt(3);
            for (int i = 0; i < lines; i++) {
                Product product = products.get(random.nextInt(products.size()));
                invoice.addLine(product, quantityFor(product, random));
            }

            int number = numberGenerator.next(SERIES, issueDate.getYear());
            invoice.issue(SERIES, issueDate.getYear(), number, issueDate);

            long age = ChronoUnit.DAYS.between(issueDate, today);
            if (!cancelledOne && age > 120 && random.nextInt(10) == 0) {
                invoice.cancel("Valores incorretos; substituída por uma nova fatura");
                cancelledOne = true;
            } else if (shouldBePaid(age, random)) {
                LocalDate paidDate = issueDate.plusDays(5 + random.nextInt(35));
                invoice.markAsPaid(paidDate.isAfter(today) ? today : paidDate);
            }

            invoiceRepository.save(invoice);
        }

        // Dois rascunhos, ainda por emitir
        for (int i = 0; i < 2; i++) {
            Invoice draft = new Invoice(clients.get(random.nextInt(clients.size())), today.plusDays(30),
                    "Proposta em preparação");
            draft.addLine(products.get(i), new BigDecimal("10"));
            invoiceRepository.save(draft);
        }

        return issueDates.size() + 2;
    }

    /** Faturas antigas estão quase todas pagas; as recentes ainda não. As que ficam por pagar ficam em atraso. */
    private static boolean shouldBePaid(long ageInDays, Random random) {
        if (ageInDays > 45) {
            return random.nextInt(10) < 9;
        }
        if (ageInDays > 15) {
            return random.nextInt(10) < 6;
        }
        return false;
    }

    private static BigDecimal quantityFor(Product product, Random random) {
        return switch (product.getCode()) {
            case "CONS-01", "DEV-01", "DES-01" ->
                    BigDecimal.valueOf(2 + random.nextInt(60)).divide(BigDecimal.valueOf(2)); // horas, de meia em meia
            case "FORM-01" -> BigDecimal.valueOf(1 + random.nextInt(2));
            case "LIV-01" -> BigDecimal.valueOf(1 + random.nextInt(10));
            case "CAT-01" -> BigDecimal.valueOf(8 + random.nextInt(18));
            default -> BigDecimal.ONE;
        };
    }

    // ---------- Despesas ----------

    private int createExpenses(LocalDate today, Random random, Map<String, Category> categories) {
        int count = 0;
        YearMonth month = YearMonth.from(today).minusMonths(11);

        while (!month.isAfter(YearMonth.from(today))) {
            count += expense(today, categories.get("Renda"), "Renda do escritório", "Imobiliária Central",
                    month.atDay(1), cents(50000), 23, PaymentMethod.TRANSFER);
            count += expense(today, categories.get("Software e subscrições"), "Ferramentas de software na cloud",
                    "Fornecedor de software", month.atDay(5), cents(5000), 23, PaymentMethod.CARD);
            count += expense(today, categories.get("Telecomunicações"), "Internet e telemóveis",
                    "Operador de telecomunicações", month.atDay(10), cents(4000), 23, PaymentMethod.TRANSFER);

            for (int i = 0, n = 2 + random.nextInt(3); i < n; i++) {
                count += expense(today, categories.get("Combustível"), "Combustível", "Posto de combustível",
                        randomDay(month, random), cents(4000 + random.nextInt(4000)), 23, PaymentMethod.CARD);
            }
            for (int i = 0, n = 1 + random.nextInt(3); i < n; i++) {
                count += expense(today, categories.get("Refeições"), "Almoço com cliente", "Restaurante",
                        randomDay(month, random), cents(1500 + random.nextInt(4500)), 13, PaymentMethod.MB_WAY);
            }
            for (int i = 0, n = random.nextInt(3); i < n; i++) {
                count += expense(today, categories.get("Deslocações"), "Viagem de comboio", "CP - Comboios",
                        randomDay(month, random), cents(2000 + random.nextInt(4000)), 6, PaymentMethod.CARD);
            }
            if (month.getMonthValue() % 3 == 0) {
                count += expense(today, categories.get("Material de escritório"), "Papel e consumíveis",
                        "Papelaria Moderna", randomDay(month, random), cents(2000 + random.nextInt(10000)), 23,
                        PaymentMethod.CASH);
            }
            if (month.getMonthValue() == 3 || month.getMonthValue() == 9) {
                count += expense(today, categories.get("Formação"), "Curso de formação técnica",
                        "Centro de formação", randomDay(month, random), cents(30000), 23, PaymentMethod.TRANSFER);
            }

            month = month.plusMonths(1);
        }
        return count;
    }

    /** Regista uma despesa com o IVA calculado sobre a base; ignora datas futuras. Devolve 1 se registou. */
    private int expense(LocalDate today, Category category, String description, String supplier,
                        LocalDate date, BigDecimal net, int vatPercent, PaymentMethod method) {
        if (date.isAfter(today)) {
            return 0;
        }
        BigDecimal vat = net.multiply(BigDecimal.valueOf(vatPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        expenseRepository.save(new Expense(category, description, supplier, null, date,
                net.add(vat), vat, method, null));
        return 1;
    }

    private static BigDecimal cents(int value) {
        return BigDecimal.valueOf(value, 2);
    }

    private static LocalDate randomDay(YearMonth month, Random random) {
        return month.atDay(1 + random.nextInt(month.lengthOfMonth()));
    }
}