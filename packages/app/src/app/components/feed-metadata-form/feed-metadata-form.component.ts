import { Component, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { GqlContentCategoryTag } from '../../../generated/graphql';

export interface FeedMetadata {
  websiteUrl: string;
  description: string;
  language: string;
  title: string;
  harvestItems: boolean;
  prerender: boolean;
  autoRelease: boolean;
}

@Component({
  selector: 'app-feed-metadata-form',
  templateUrl: './feed-metadata-form.component.html',
  styleUrls: ['./feed-metadata-form.component.scss'],
})
export class FeedMetadataFormComponent implements OnInit {
  @Input()
  data: FeedMetadata;
  @Input()
  readOnly: boolean;

  @Input()
  showPrerenderOption: boolean;

  formGroup: FormGroup<{
    websiteUrl: FormControl<string>;
    description: FormControl<string | null>;
    language: FormControl<string | null>;
    title: FormControl<string>;
    tags: FormControl<string[]>;
    harvestItems: FormControl<boolean>;
    prerender: FormControl<boolean>;
    autoRelease: FormControl<boolean>;
  }>;
  categoryTags: string[];

  constructor() {}

  ngOnInit() {
    this.categoryTags = Object.values(GqlContentCategoryTag);
    this.formGroup = new FormGroup({
      title: new FormControl(this.data?.title || '', Validators.required),
      description: new FormControl(this.data?.description || ''),
      websiteUrl: new FormControl(
        this.data?.websiteUrl || '',
        Validators.required
      ),
      language: new FormControl(this.data?.language || '', [Validators.max(3)]),
      tags: new FormControl([]),
      harvestItems: new FormControl(
        this.data?.harvestItems || false,
        Validators.required
      ),
      prerender: new FormControl(
        this.data?.prerender || false,
        Validators.required
      ),
      autoRelease: new FormControl(
        this.data?.autoRelease || false,
        Validators.required
      ),
    });
  }
}
