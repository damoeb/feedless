import { Component, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

export interface FeedMetadata {
  websiteUrl: string;
  description: string;
  language: string;
  title: string;
  harvestItems: boolean;
  prerender: boolean;
}

@Component({
  selector: 'app-importer-metadata-form',
  templateUrl: './importer-metadata-form.component.html',
  styleUrls: ['./importer-metadata-form.component.scss'],
})
export class ImporterMetadataFormComponent
  implements OnInit
{
  @Input()
  data: FeedMetadata;

  @Input()
  showPrerenderOption: boolean;

  formGroup: FormGroup<{
    autoImport: FormControl<boolean>;
  }>;

  constructor() {}

  ngOnInit() {
    this.formGroup = new FormGroup({
      autoImport: new FormControl(true, Validators.required),
    });
  }
}
