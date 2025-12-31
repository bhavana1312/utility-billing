import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-tariffs',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './tariffs.html',
  styleUrl: './tariffs.css',
})
export class Tariffs {
  utilities = ['Water', 'Electricity', 'Gas'];
  plans = ['Domestic', 'Commercial', 'Industrial'];
  selectedUtility = 'Water';

  constructor(private router: Router, private sanitizer: DomSanitizer) {}

  getPlanDescription(plan: string): string {
    const descriptions: Record<string, string> = {
      Domestic: 'For residential households',
      Commercial: 'For businesses and offices',
      Industrial: 'For manufacturing units',
    };
    return descriptions[plan] || '';
  }

  getUtilityIcon(utility: string): SafeHtml {
    const icons: Record<string, string> = {
      Water: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/></svg>`,
      Electricity: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>`,
      Gas: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2c1.2 0 3 .8 3 4.5S12 14 12 14s-3-3.8-3-7.5S10.8 2 12 2z"/><path d="M19 15.5c-1 1.5-3 2.5-5 2.5s-4-1-5-2.5"/></svg>`,
    };
    return this.sanitizer.bypassSecurityTrustHtml(icons[utility] || '');
  }

  getPlanIcon(plan: string): SafeHtml {
    const icons: Record<string, string> = {
      Domestic: `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>`,
      Commercial: `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="16" height="20" x="4" y="2" rx="2"/><path d="M9 22v-4h6v4"/><path d="M8 6h.01"/><path d="M16 6h.01"/><path d="M8 10h.01"/><path d="M16 10h.01"/><path d="M8 14h.01"/><path d="M16 14h.01"/></svg>`,
      Industrial: `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 20V9l4-2v13"/><path d="M18 20V7l4-2v15"/><path d="M10 20V5l4-2v17"/><line x1="2" x2="22" y1="20" y2="20"/></svg>`,
    };
    return this.sanitizer.bypassSecurityTrustHtml(icons[plan] || '');
  }

  explore(utility: string, plan: string) {
    this.router.navigate(['/tariffs', utility.toUpperCase(), plan.toUpperCase()]);
  }
}
