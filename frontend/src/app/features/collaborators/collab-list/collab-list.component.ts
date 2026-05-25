import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CollaboratorService } from '../../../core/services/collaborator.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { AuthService } from '../../../core/services/auth.service';
import { Collaborator } from '../../../core/models/collaborator.model';
import { Organization } from '../../../core/models/organization.model';

@Component({
  selector: 'app-collab-list',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './collab-list.component.html',
  styleUrl: './collab-list.component.css'
})
export class CollabListComponent implements OnInit {
  private svc  = inject(CollaboratorService);
  private orgSvc = inject(OrganizationService);
  private fb   = inject(FormBuilder);
  auth = inject(AuthService);

  collabs = signal<Collaborator[]>([]);
  orgs    = signal<Organization[]>([]);
  loading = signal(true);
  saving  = signal(false);
  showModal    = signal(false);
  editTarget   = signal<Collaborator | null>(null);
  error        = signal('');
  deleteConfirm = signal<number | null>(null);

  form = this.fb.group({
    name:           ['', [Validators.required, Validators.maxLength(100)]],
    cpf:            ['', [Validators.required, Validators.maxLength(14)]],
    email:          ['', [Validators.required, Validators.email]],
    position:       ['', [Validators.required, Validators.maxLength(50)]],
    organizationId: [null as number | null, Validators.required]
  });

  ngOnInit(): void {
    this.load();
    this.orgSvc.findAll().subscribe({ next: data => this.orgs.set(data) });
  }

  load(): void {
    this.loading.set(true);
    this.svc.findAll().subscribe({
      next: data => { this.collabs.set(data); this.loading.set(false); },
      error: ()   => this.loading.set(false)
    });
  }

  openCreate(): void {
    this.editTarget.set(null);
    this.form.reset({ organizationId: this.auth.currentOrganizationId() });
    this.error.set('');
    this.showModal.set(true);
  }

  openEdit(c: Collaborator): void {
    this.editTarget.set(c);
    this.form.patchValue({
      name: c.name, cpf: c.cpf, email: c.email,
      position: c.position, organizationId: c.organizationId
    });
    this.error.set('');
    this.showModal.set(true);
  }

  closeModal(): void { this.showModal.set(false); }

  save(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    this.error.set('');
    const payload = this.form.value as any;
    const target  = this.editTarget();
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
        this.error.set(err.error?.message || 'Não foi possível excluir o colaborador. Verifique se ele possui vínculos ativos.');
      }
    });
  }
}
