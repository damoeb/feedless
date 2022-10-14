import { Component, OnInit } from '@angular/core';
import { BucketService } from '../../services/bucket.service';
import { GqlBucketVisibility } from '../../../generated/graphql';
import { Router } from '@angular/router';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-bucket-create',
  templateUrl: './bucket-create.page.html',
  styleUrls: ['./bucket-create.page.scss'],
})
export class BucketCreatePage implements OnInit {
  formGroup: FormGroup<{ website: FormControl<string | null>; description: FormControl<string | null>; name: FormControl<string | null> }>;

  constructor(private readonly bucketService: BucketService,
              private readonly router: Router) {
    this.formGroup = new FormGroup({
      name: new FormControl('', Validators.required),
      description: new FormControl('', Validators.required),
      website: new FormControl('', Validators.required),
    });
  }

  ngOnInit() {
  }

  async createBucket() {
    // console.log(this.formGroup);
    // if(!this.formGroup.invalid) {
    //   const bucket = await this.bucketService.createBucket({
    //     name: 'this.name',
    //     description: 'this.description',
    //     releaseManually: false,
    //     visibility: GqlBucketVisibility.IsPublic
    //   });
    //   await this.router.navigateByUrl(`/bucket/${bucket.id}/edit`)
    // }
      await this.router.navigateByUrl(`/bucket/35e266ed-65e9-437e-bf05-54786d264d57`)
  }
}
