import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ConsumerSidebar } from '../consumer-sidebar/consumer-sidebar';

@Component({
  standalone: true,
  imports: [CommonModule, ConsumerSidebar],
  templateUrl: './connections.html',
  styleUrl: './connections.css',
})
export class Connections {
  @ViewChild(ConsumerSidebar) sidebar!: ConsumerSidebar;

  isSidebarCollapsed = false;
  today = new Date();
  meters: any[] = [];

  consumerId = localStorage.getItem('consumerId');

  constructor(private http: HttpClient) {
    this.loadMeters();
  }

  onSidebarToggle(val: boolean) {
    this.isSidebarCollapsed = val;
  }

  loadMeters() {
    console.log(this.consumerId);
    this.http
      .get<any[]>(`http://localhost:9090/meters/consumer/${this.consumerId}`)
      .subscribe((res) => (this.meters = res));
  }
}
