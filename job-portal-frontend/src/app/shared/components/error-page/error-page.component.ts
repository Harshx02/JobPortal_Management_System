import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-error-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen flex flex-col items-center justify-center bg-gray-50 dark:bg-gray-900 px-4">
      <div class="text-center">
        <h1 class="text-9xl font-extrabold text-blue-600 tracking-widest">{{ code }}</h1>
        <div class="bg-indigo-600 px-2 text-sm rounded rotate-12 absolute transform -translate-y-12 translate-x-32 inline-block text-white">
          {{ title }}
        </div>
        <div class="mt-5">
          <p class="text-2xl font-semibold text-gray-700 dark:text-gray-300 md:text-3xl mt-8">{{ message }}</p>
          <p class="mt-4 text-gray-500 dark:text-gray-400">Sorry, we couldn't find the page you're looking for.</p>
          
          <div class="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
            <a routerLink="/home" class="btn-primary px-8 py-3 text-lg">
              Go Home
            </a>
            <button (click)="goBack()" class="px-8 py-3 text-lg border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition">
              Previous Page
            </button>
          </div>
        </div>
      </div>
      
      <!-- Subtle background decoration -->
      <div class="absolute inset-0 pointer-events-none overflow-hidden opacity-10">
        <div class="absolute -top-24 -left-24 w-96 h-96 bg-blue-400 rounded-full blur-3xl"></div>
        <div class="absolute -bottom-24 -right-24 w-96 h-96 bg-indigo-400 rounded-full blur-3xl"></div>
      </div>
    </div>
  `
})
export class ErrorPageComponent implements OnInit {
  code: string = '404';
  title: string = 'Page Not Found';
  message: string = "Oops! You've reached a dead end.";

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.data.subscribe(data => {
      this.code = data['code'] || '404';
      this.title = data['title'] || 'Error';
      this.message = data['message'] || "Something went wrong.";
    });
  }

  goBack() {
    window.history.back();
  }
}
