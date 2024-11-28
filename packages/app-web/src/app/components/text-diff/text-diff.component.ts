import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  Input,
  input,
  OnInit,
  viewChild,
} from '@angular/core';
import { Record } from '../../graphql/types';
import { dateFormat } from '../../services/session.service';
import { GqlRecordField } from '../../../generated/graphql';
import { isUndefined } from 'lodash-es';
import { CodeEditorComponent } from '../../elements/code-editor/code-editor.component';

@Component({
  selector: 'app-text-diff',
  templateUrl: './text-diff.component.html',
  styleUrls: ['./text-diff.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CodeEditorComponent],
  standalone: true,
})
export class TextDiffComponent implements OnInit {
  private readonly changeRef = inject(ChangeDetectorRef);

  @Input({ required: true })
  before: Record;

  @Input()
  after: Record;

  readonly compareBy = input<GqlRecordField>(GqlRecordField.Markup);

  readonly diffEditorComponent = viewChild<CodeEditorComponent>('diffEditor');

  protected textBefore: string;
  protected textAfter: string;
  protected readonly dateFormat = dateFormat;
  protected highlightedLines: number[];
  compareByText = GqlRecordField.Text;
  compareByMarkup = GqlRecordField.Markup;

  async ngOnInit() {
    const textBefore = this.getText(this.before);
    const textAfter = this.getText(this.after);

    const textBeforeLines = textBefore.split(/\n/);
    const textAfterLines = textAfter.split(/\n/);

    this.highlightedLines = textAfterLines
      .map((line, index) => {
        if (line !== textBeforeLines[index]) {
          return index + 1;
        }
      })
      .filter((lineNr) => !isUndefined(lineNr));

    this.textBefore = textBefore;
    this.textAfter = textAfter;
    this.changeRef.detectChanges();
  }

  private getText(document: Record): string {
    switch (this.compareBy()) {
      case GqlRecordField.Markup:
        return this.formatHtml(document.html);
      case GqlRecordField.Text:
        return document.text;
    }
    throw Error();
  }

  private formatHtml(html: string) {
    const tab = '\t';
    let result = '';
    let indent = '';

    html.split(/>\s*</).forEach((element) => {
      if (element.match(/^\/\w/)) {
        indent = indent.substring(tab.length);
      }

      result += indent + '<' + element + '>\r\n';

      if (element.match(/^<?\w[^>]*[^\/]$/) && !element.startsWith('input')) {
        indent += tab;
      }
    });

    return result.substring(1, result.length - 3);
  }

  handleScroll(event: { left: number; top: number }) {
    console.log(event);
  }
}
