import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * COMPONENTE RAIZ - a "moldura" da aplicação: só mostra a página da rota atual.
 *
 * Fala com:     RouterOutlet
 * É usado por:  main.ts (é o primeiro componente a ser desenhado)
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {}