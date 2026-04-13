import { CanDeactivateFn } from '@angular/router';

export interface HasUnsavedChanges {
  hasUnsavedChanges(): boolean;
}

export const deactivateGuard: CanDeactivateFn<HasUnsavedChanges> = (component) => {
  if (component.hasUnsavedChanges && component.hasUnsavedChanges()) {
    return confirm('You have unsaved changes! Do you really want to leave?');
  }
  return true;
};
