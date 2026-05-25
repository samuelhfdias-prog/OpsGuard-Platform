import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { OrganizationService } from '../../../core/services/organization.service';
import { AuthService } from '../../../core/services/auth.service';
import { Organization } from '../../../core/models/organization.model';

@Component({
  selector: 'app-org-list',
  standalone: true,
  imports: [ReactiveFormsModule, SlicePipe],
  templateUrl: './org-list.component.html',
  styleUrl: './org-list.component.css'
})
export class OrgListComponent implements OnInit {
  private svc = inject(OrganizationService);
  private fb = inject(FormBuilder);
  auth = inject(AuthService);

  orgs = signal<Organization[]>([]);
  loading = signal(true);
  saving = signal(false);
  showModal = signal(false);
  editTarget = signal<Organization | null>(null);
  error = signal('');
  deleteConfirm = signal<number | null>(null);

  form = this.fb.group({
    name:    ['', [Validators.required, Validators.maxLength(100)]],
    cnpj:    ['', [Validators.required, Validators.maxLength(18)]],
    address: ['', Validators.maxLength(200)]
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.svc.findAll().subscribe({
      next: data => { this.orgs.set(data); this.loading.set(false); },
      error: ()   => this.loading.set(false)
    });
  }

  openCreate(): void {
    this.editTarget.set(null);
    this.form.reset();
    this.error.set('');
    this.showModal.set(true);
  }

  openEdit(org: Organization): void {
    this.editTarget.set(org);
    this.form.patchValue({ name: org.name, cnpj: org.cnpj, address: org.address ?? '' });
    this.error.set('');
    this.showModal.set(true);
  }

  closeModal(): void { this.showModal.set(false); }

  save(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    this.error.set('');
    const payload = this.form.value as any;
    const target = this.editTarget();
    const req$ = target
      ? this.svc.update(target.id, payload)
      : this.svc.create(payload);

    req$.subscribe({
      next: () => { this.load(); this.closeModal(); this.saving.set(false); },
      error: (err) => { this.error.set(err.error?.message || 'Erro ao salvar'); this.saving.set(false); }
    });
  }

  confirmDelete(id: number): void { this.deleteConfirm.set(id); }
  cancelDelete(): void { this.deleteConfirm.set(null); }

  delete(id: number): void {
    this.svc.delete(id).subscribe({
      next: () => { this.load(); this.deleteConfirm.set(null); },
      error: (err) => {
        this.deleteConfirm.set(null);
        this.error.set(err.error?.message || 'Não foi possível excluir a organização. Verifique se ela possui colaboradores ou dispositivos vinculados.');
      }
    });
  }
}
