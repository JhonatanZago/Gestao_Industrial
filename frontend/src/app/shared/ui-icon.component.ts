import { Component, input } from '@angular/core';

@Component({
  selector: 'ui-icon',
  standalone: true,
  template: `
    <svg class="icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      @switch (name()) {
        @case ('brand') { <path d="M3 20h18M5 20V10l5 3V8l5 3V5h4v15M8 16h1m4 0h1m4 0h1M16 5V3h2v2"/> }
        @case ('dashboard') { <path d="M4 13h6V4H4v9Zm0 7h6v-4H4v4Zm10 0h6v-9h-6v9Zm0-16v4h6V4h-6Z"/> }
        @case ('sectors') { <path d="M4 20h16M6 20V8l6-4 6 4v12M9 20v-5h6v5M9 10h.01M15 10h.01"/> }
        @case ('import') { <path d="M12 3v12m0 0 4-4m-4 4-4-4M5 21h14a2 2 0 0 0 2-2v-3M3 16v3a2 2 0 0 0 2 2"/> }
        @case ('report') { <path d="M5 4h14v16H5zM8 8h8M8 12h8M8 16h5"/> }
        @case ('production') { <path d="M4 19V9m5 10V5m6 14v-7m5 7V3"/> }
        @case ('people') { <path d="M16 20v-1a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v1m6-9a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm6-5a3 3 0 0 1 2 5m0 4h2"/> }
        @case ('parts') { <path d="m12 3 8 4.5v9L12 21l-8-4.5v-9L12 3Zm0 0v9m8-4.5-8 4.5m0 0L4 7.5"/> }
        @case ('refresh') { <path d="M20 11a8 8 0 0 0-14.9-3M4 5v4h4M4 13a8 8 0 0 0 14.9 3M20 19v-4h-4"/> }
        @case ('menu') { <path d="M4 6h16M4 12h16M4 18h16"/> }
        @case ('logout') { <path d="M10 5H5v14h5m5-4 4-3-4-3m4 3H9"/> }
        @case ('arrow') { <path d="M5 12h14m-5-5 5 5-5 5"/> }
        @case ('check') { <path d="m5 12 4 4L19 6"/> }
        @case ('close') { <path d="M6 6l12 12M18 6 6 18"/> }
        @case ('excel') { <path d="M7 3h7l4 4v14H7V3Zm7 0v5h5M4 8l6 8M10 8l-6 8"/> }
        @case ('download') { <path d="M12 3v12m0 0 4-4m-4 4-4-4M5 21h14"/> }
        @case ('warning') { <path d="M12 3 2.7 20h18.6L12 3Zm0 6v5m0 3h.01"/> }
        @case ('clock') { <circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/> }
        @default { <circle cx="12" cy="12" r="8"/> }
      }
    </svg>
  `,
  styles: [`:host{display:inline-block;width:1.15rem;height:1.15rem;line-height:0}.icon{display:block;width:100%;height:100%;fill:none;stroke:currentColor;stroke-width:1.8;stroke-linecap:round;stroke-linejoin:round}`]
})
export class UiIconComponent { readonly name = input('default'); }
