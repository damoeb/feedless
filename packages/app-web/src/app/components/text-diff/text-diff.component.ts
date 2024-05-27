import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, ViewChild } from '@angular/core';
import { WebDocument } from '../../graphql/types';
import { dateFormat } from '../../services/session.service';
import { GqlWebDocumentField } from '../../../generated/graphql';
import { isUndefined } from 'lodash-es';
import { CodeEditorComponent } from '../../elements/code-editor/code-editor.component';

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

  @ViewChild('diffEditor')
  diffEditorComponent: CodeEditorComponent

  protected textBefore: string;
  protected textAfter: string;
  protected readonly dateFormat = dateFormat;
  protected highlightedLines: number[];
  compareByText = GqlWebDocumentField.Text
  compareByMarkup = GqlWebDocumentField.Markup

  constructor(
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    const textBefore = this.getText(this.before)
    const textAfter = this.getText(this.after)

    const textBeforeLines = textBefore.split(/\n/)
    const textAfterLines = textAfter.split(/\n/)

    this.highlightedLines = textAfterLines.map((line, index) => {
      if (line !== textBeforeLines[index]) {
        return index + 1;
      }
    }).filter(lineNr => !isUndefined(lineNr))

    this.textBefore = textBefore;
    this.textAfter = textAfter;
    this.changeRef.detectChanges();
  }

  private getText(document: WebDocument): string {
    switch (this.compareBy) {
      case GqlWebDocumentField.Markup: return this.formatHtml(document.contentHtml);
      case GqlWebDocumentField.Text: return document.contentText;
    }
    throw Error()
  }

  private formatHtml(html) {
    const tab = '\t';
    let result = '';
    let indent = '';

    html.split(/>\s*</).forEach(function(element) {
      if (element.match( /^\/\w/ )) {
        indent = indent.substring(tab.length);
      }

      result += indent + '<' + element + '>\r\n';

      if (element.match( /^<?\w[^>]*[^\/]$/ ) && !element.startsWith("input")  ) {
        indent += tab;
      }
    });

    return result.substring(1, result.length-3);
  }

  handleScroll(event: { left: number; top: number }) {
    console.log(event)
  }
}
