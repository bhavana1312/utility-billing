import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormArray,
  FormGroup,
} from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { AdminSidebar } from '../admin-sidebar/admin-sidebar';

@Component({
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, AdminSidebar],
  templateUrl: './manage-utilities.html',
  styleUrl: './manage-utilities.css',
})
export class ManageUtilities {
  utilities = ['WATER', 'ELECTRICITY', 'GAS'];
  plans = ['DOMESTIC', 'COMMERCIAL', 'INDUSTRIAL'];

  selectedUtility = 'WATER';
  selectedPlan = 'DOMESTIC';

  today = '';
  editMode = false;

  tariff: any = null;
  loading = false;

  form: FormGroup;

  constructor(private fb: FormBuilder, private http: HttpClient, private toast: ToastrService) {
    this.today = new Date().toISOString().split('T')[0];

    this.form = this.fb.group({
      fixedCharge: [0, [Validators.required, Validators.min(0)]],
      taxPercentage: [0, [Validators.required, Validators.min(0)]],
      effectiveFrom: [this.today, Validators.required],
      slabs: this.fb.array([]),
      overduePenaltySlabs: this.fb.array([]),
    });
  }

  ngOnInit() {
    this.loadTariff();
  }

  get slabs() {
    return this.form.get('slabs') as FormArray;
  }

  get overduePenaltySlabs() {
    return this.form.get('overduePenaltySlabs') as FormArray;
  }

  get slabGroups() {
    return this.slabs.controls as FormGroup[];
  }

  get penaltyGroups() {
    return this.overduePenaltySlabs.controls as FormGroup[];
  }

  addSlab() {
    this.slabs.push(
      this.fb.group({
        fromUnit: [0, Validators.required],
        toUnit: [0, Validators.required],
        ratePerUnit: [0, Validators.required],
      })
    );
  }

  removeSlab(i: number) {
    this.slabs.removeAt(i);
  }

  addPenaltySlab() {
    this.overduePenaltySlabs.push(
      this.fb.group({
        fromDay: [0, Validators.required],
        toDay: [0, Validators.required],
        penaltyPercentage: [0, Validators.required],
      })
    );
  }

  removePenaltySlab(i: number) {
    this.overduePenaltySlabs.removeAt(i);
  }

  loadTariff() {
    if (!this.selectedUtility || !this.selectedPlan) return;

    this.loading = true;

    this.http
      .get(
        `http://localhost:9090/utilities/tariffs/${this.selectedUtility}/plans/${this.selectedPlan}`
      )
      .subscribe({
        next: (res: any) => {
          this.tariff = res;

          this.form.patchValue({
            fixedCharge: res.fixedCharge,
            taxPercentage: res.taxPercentage,
            effectiveFrom: res.effectiveFrom,
          });

          this.slabs.clear();
          res.slabs.forEach((s: any) => {
            this.slabs.push(
              this.fb.group({
                fromUnit: [s.fromUnit],
                toUnit: [s.toUnit],
                ratePerUnit: [s.ratePerUnit],
              })
            );
          });

          this.overduePenaltySlabs.clear();
          res.overduePenaltySlabs.forEach((p: any) => {
            this.overduePenaltySlabs.push(
              this.fb.group({
                fromDay: [p.fromDay],
                toDay: [p.toDay],
                penaltyPercentage: [p.penaltyPercentage],
              })
            );
          });

          this.form.disable();
          this.editMode = false;
          this.loading = false;
        },
        error: () => {
          this.tariff = null;
          this.form.reset({ effectiveFrom: this.today });
          this.slabs.clear();
          this.overduePenaltySlabs.clear();
          this.form.enable();
          this.editMode = true;
          this.loading = false;
          this.toast.info('No active tariff found. Create a new one.');
        },
      });
  }

  enableEdit() {
    this.editMode = true;
    this.form.enable();
    this.form.patchValue({
      effectiveFrom: this.today,
    });
  }

  cancelEdit() {
    this.editMode = false;
    this.form.disable();
    this.loadTariff();
  }

  save() {
    if (this.form.invalid) {
      this.toast.warning('Fix validation errors');
      return;
    }

    const payload = {
      utilityType: this.selectedUtility,
      plan: this.selectedPlan,
      active: true,
      ...this.form.value,
    };

    console.log(this.tariff);

    const req = this.tariff
      ? this.http.put(
          `http://localhost:9090/utilities/tariffs/${this.selectedUtility}/plans/${this.selectedPlan}`,
          payload
        )
      : this.http.post(`http://localhost:9090/utilities/tariffs/plans`, payload);

    req.subscribe({
      next: () => {
        this.toast.success(this.tariff ? 'Tariff updated' : 'Tariff created');
        this.loadTariff();
      },
      error: (err) => {
        this.toast.error(err?.error?.message || 'Operation failed');
      },
    });
  }

  deactivate() {
    this.http
      .delete(
        `http://localhost:9090/utilities/tariffs/${this.selectedUtility}/plans/${this.selectedPlan}`
      )
      .subscribe({
        next: () => {
          this.toast.success('Tariff deactivated');
          this.tariff = null;
          this.form.reset({ effectiveFrom: this.today });
          this.slabs.clear();
          this.overduePenaltySlabs.clear();
          this.form.enable();
          this.editMode = true;
        },
        error: () => this.toast.error('Deactivation failed'),
      });
  }
}
