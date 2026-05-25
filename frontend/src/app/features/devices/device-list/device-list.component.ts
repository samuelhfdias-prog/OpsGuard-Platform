import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DeviceService } from '../../../core/services/device.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { AuthService } from '../../../core/services/auth.service';
import { Device, DeviceType, DEVICE_TYPE_LABELS } from '../../../core/models/device.model';
import { Organization } from '../../../core/models/organization.model';

@Component({
  selector: 'app-device-list',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './device-list.component.html',
  styleUrl: './device-list.component.css'
})
export class DeviceListComponent implements OnInit {
  private svc    = inject(DeviceService);
  private orgSvc = inject(OrganizationService);
  private fb     = inject(FormBuilder);
  auth = inject(AuthService);

  devices  = signal<Device[]>([]);
  orgs     = signal<Organization[]>([]);
  loading  = signal(true);
  saving   = signal(false);
  showModal     = signal(false);
  editTarget    = signal<Device | null>(null);
  error         = signal('');
  deleteConfirm = signal<number | null>(null);

  readonly deviceTypes: DeviceType[] = ['SMARTPHONE','TABLET','LAPTOP','TRACKER','OTHER'];
  readonly labels = DEVICE_TYPE_LABELS;

  form = this.fb.group({
    name:           ['', [Validators.required, Validators.maxLength(100)]],
    serialNumber:   ['', [Validators.required, Validators.maxLength(50)]],
    type:           ['SMARTPHONE' as DeviceType, Validators.required],
    organizationId: [null as number | null, Validators.required]
  });

  ngOnInit(): void {
    this.load();
    this.orgSvc.findAll().subscribe({ next: data => this.orgs.set(data) });
  }

  load(): void {
    this.loading.set(true);
    this.svc.findAll().subscribe({
      next: data => { this.devices.set(data); this.loading.set(false); },
      error: ()   => this.loading.set(false)
    });
  }

  openCreate(): void {
    this.editTarget.set(null);
    this.form.reset({ type: 'SMARTPHONE', organizationId: this.auth.currentOrganizationId() });
    this.error.set('');
    this.showModal.set(true);
  }

  openEdit(d: Device): void {
    this.editTarget.set(d);
    this.form.patchValue({
      name: d.name, serialNumber: d.serialNumber,
      type: d.type, organizationId: d.organizationId
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
        this.error.set(err.error?.message || 'Não foi possível excluir o dispositivo. Verifique se ele possui vínculos ativos.');
      }
    });
  }

  badgeClass(type: DeviceType): string {
    return 'badge badge-' + type.toLowerCase();
  }
}
