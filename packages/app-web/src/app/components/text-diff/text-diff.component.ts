import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { WebDocument } from '../../graphql/types';
import { dateFormat, dateTimeFormat } from '../../services/session.service';
import { GqlWebDocumentField } from '../../../generated/graphql';

@Component({
  selector: 'app-text-diff',
  templateUrl: './text-diff.component.html',
  styleUrls: ['./text-diff.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TextDiffComponent implements OnInit {
  @Input({required: true})
  before: WebDocument;

  @Input()
  after: WebDocument;

  @Input()
  compareBy: GqlWebDocumentField = GqlWebDocumentField.Markup

  compareByText = GqlWebDocumentField.Text
  compareByMarkup = GqlWebDocumentField.Markup

  constructor(
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {

    this.changeRef.detectChanges();
  }

  protected readonly dateFormat = dateFormat;
  protected readonly dateTimeFormat = dateTimeFormat;
}
