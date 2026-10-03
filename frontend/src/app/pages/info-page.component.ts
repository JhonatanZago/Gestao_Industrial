import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({ standalone: true, template: `<main style="min-height:100vh;display:grid;place-items:center;background:#0b1214;color:#e5f0ed;font-family:system-ui"><section style="text-align:center"><p style="color:#44d7a0;letter-spacing:.15em">METALÚRGICA ERP</p><h1>{{ title }}</h1><p>A tela é controlada pelo shell principal.</p></section></main>` })
export class InfoPageComponent { readonly title = inject(ActivatedRoute).snapshot.data['title'] as string; }
