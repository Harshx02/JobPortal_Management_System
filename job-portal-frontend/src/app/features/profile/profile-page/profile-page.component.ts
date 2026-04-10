import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NavbarComponent } from '../../../shared/components/navbar/navbar.component';
import { ProfileService } from '../../../core/services/profile.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserResponse, UpdateProfileRequest } from '../../../core/models/auth.model';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  templateUrl: './profile-page.component.html'
})
export class ProfilePageComponent implements OnInit {
  private profileService = inject(ProfileService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);

  profile = signal<UserResponse | null>(null);
  loading = signal(true);
  error = signal('');
  success = signal('');

  isEditing = signal(false);
  editForm: FormGroup;
  uploadingImage = signal(false);

  constructor() {
    this.editForm = this.fb.group({
      name: ['', Validators.required],
      phone: [''],
      location: [''],
      skills: [''],
      bio: ['']
    });
  }

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.loading.set(true);
    this.error.set('');
    
    this.profileService.getProfile().subscribe({
      next: (res) => {
        this.profile.set(res);
        this.loading.set(false);
        this.populateForm(res);
      },
      error: (err) => {
        this.error.set('Failed to load profile.');
        this.loading.set(false);
      }
    });
  }

  populateForm(data: UserResponse) {
    this.editForm.patchValue({
      name: data.name,
      phone: data.phone || '',
      location: data.location || '',
      skills: data.skills || '',
      bio: data.bio || ''
    });
  }

  toggleEdit() {
    this.isEditing.update(v => !v);
    if (!this.isEditing() && this.profile()) {
      this.populateForm(this.profile()!);
    }
  }

  saveProfile() {
    if (this.editForm.invalid) return;
    
    this.loading.set(true);
    this.error.set('');
    this.success.set('');

    const payload: UpdateProfileRequest = this.editForm.value;

    this.profileService.updateProfile(payload).subscribe({
      next: (res) => {
        this.profile.set(res);
        this.success.set('Profile updated successfully!');
        this.isEditing.set(false);
        this.loading.set(false);
        setTimeout(() => this.success.set(''), 3000);
      },
      error: (err) => {
        this.error.set('Failed to update profile.');
        this.loading.set(false);
      }
    });
  }

  onImageSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Must use userId from local storage or auth response
      const userId = this.authService.userId() || this.profile()?.id;
      if (!userId) {
        this.error.set('User ID not found. Please load your profile first.');
        return;
      }

      this.uploadingImage.set(true);
      this.profileService.uploadProfileImage(userId, file).subscribe({
        next: (res) => {
          this.profile.update(p => p ? { ...p, profileImageUrl: res.profileImageUrl } : null);
          this.uploadingImage.set(false);
          this.success.set('Profile image updated!');
          setTimeout(() => this.success.set(''), 3000);
        },
        error: (err) => {
          this.error.set('Failed to upload image.');
          this.uploadingImage.set(false);
        }
      });
    }
  }
}
