import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProfileService } from './profile.service';
import { environment } from '../../../environments/environment';
import { UserResponse, UpdateProfileRequest } from '../models/auth.model';

describe('ProfileService', () => {
  let service: ProfileService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/api/auth`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProfileService]
    });
    service = TestBed.inject(ProfileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get profile', () => {
    const mockUser: Partial<UserResponse> = { id: 1, name: 'Test User' };

    service.getProfile().subscribe(user => {
      expect(user.name).toBe('Test User');
    });

    const req = httpMock.expectOne(`${apiUrl}/profile`);
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });

  it('should update profile', () => {
    const updateDto: UpdateProfileRequest = { name: 'Updated Name', bio: 'Updated Bio' };
    const mockRes: Partial<UserResponse> = { id: 1, ...updateDto };

    service.updateProfile(updateDto).subscribe(user => {
      expect(user.name).toBe('Updated Name');
    });

    const req = httpMock.expectOne(`${apiUrl}/users/profile`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockRes);
  });

  it('should upload profile image', () => {
    const mockFile = new File(['image content'], 'profile.jpg', { type: 'image/jpeg' });
    const mockRes = { profileImageUrl: 'http://cdn.com/img.jpg' };

    service.uploadProfileImage(1, mockFile).subscribe(res => {
      expect(res.profileImageUrl).toBe(mockRes.profileImageUrl);
    });

    const req = httpMock.expectOne(`${apiUrl}/users/1/profile-image`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTruthy();
    req.flush(mockRes);
  });
});
