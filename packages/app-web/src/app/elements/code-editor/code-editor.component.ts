import { AfterViewInit, Component, ElementRef, EventEmitter, Input, Output, ViewChild, ViewEncapsulation } from '@angular/core';

import { EditorState, Extension, StateField } from '@codemirror/state';
import {
  drawSelection,
  EditorView,
  gutter,
  highlightActiveLine,
  highlightSpecialChars,
  keymap,
  lineNumbers,
  showTooltip,
  Tooltip
} from '@codemirror/view';
import { bracketMatching, foldGutter, foldKeymap, indentOnInput } from '@codemirror/language';
import { highlightSelectionMatches } from '@codemirror/search';
import {
  autocompletion,
  closeBrackets,
  closeBracketsKeymap,
  Completion,
  CompletionContext,
  completionKeymap,
  CompletionResult,
  startCompletion
} from '@codemirror/autocomplete';


import { markdownLanguageSupport } from './markdown.lang';
import { theme } from './theme';
import { markdownDecorator } from './markdown.decorator';
import { hashtagMatcher } from './hashtag.widget';
import { urlDecorator } from './url.decorator';
import { lintKeymap } from '@codemirror/lint';
import { defaultKeymap, historyKeymap } from '@codemirror/commands';
import { inlineImagePlugin } from './inline-image.widget';
import { checkboxPlugin } from './checkbox.widget';
import { noteReferenceMatcher } from './note-reference.widget';

function getCursorTooltips(state: EditorState): readonly Tooltip[] {
  if (true) {
    return []
  }
  return state.selection.ranges
    .filter(range => range.empty)
    .map(range => {
      let line = state.doc.lineAt(range.head)
      let text = line.number + ":" + (range.head - line.from)
      return {
        pos: range.head,
        above: true,
        strictSide: true,
        arrow: true,
        create: () => {
          const dom = document.createElement("div")
          dom.className = "cm-tooltip-cursor"
          // dom.textContent = text
          const ahref = document.createElement("a");
          ahref.setAttribute("href", "");
          ahref.text = "Link"

          dom.append(ahref)
          return {dom}
        }
      }
    })
}


const cursorTooltipField = StateField.define<readonly Tooltip[]>({
  create: getCursorTooltips,

  update(tooltips, tr) {
    if (!tr.docChanged && !tr.selection) return tooltips
    return getCursorTooltips(tr.state)
  },

  provide: f => showTooltip.computeN([f], state => state.field(f))
})


//   {label: "match", apply: 'karli'},
//   {label: "hello", info: "(World)"},
export type AutoSuggestionsProvider = (query: string) => Completion[]

@Component({
  selector: 'app-code-editor',
  templateUrl: './code-editor.component.html',
  styleUrls: ['./code-editor.component.scss'],
  encapsulation: ViewEncapsulation.ShadowDom
})
export class CodeEditorComponent implements AfterViewInit
{
  @ViewChild('editor')
  editor!: ElementRef<HTMLDivElement>

  @Input({required: true})
  text: string

  @Input()
  readOnly: boolean = false

  @Output()
  textChange = new EventEmitter<string>();

  @Input()
  autoSuggestionsProvider: AutoSuggestionsProvider = () => [];

  private editorView: EditorView;

  constructor() {

  }

  ngAfterViewInit() {
    this.setText(this.text);
  }

  private getExtensions() {
    const textChangeHook = this.textChange;
    const extensions: Extension[] = [
      gutter({
        renderEmptyElements: true
      }),
      EditorView.updateListener.of(update => {
        if (update.docChanged) {
          textChangeHook.emit(this.getText())
        }
      }),
      lineNumbers(),
      EditorState.readOnly.of(this.readOnly),
      highlightSpecialChars(),
      foldGutter(),
      drawSelection(),
      EditorState.allowMultipleSelections.of(true),
      indentOnInput(),
      bracketMatching(),
      closeBrackets(),
      highlightActiveLine(),
      // rectangularSelection(),
      highlightSelectionMatches(),
      hashtagMatcher,
      noteReferenceMatcher,
      EditorView.lineWrapping,
      keymap.of([
        ...closeBracketsKeymap,
        ...defaultKeymap,
        ...historyKeymap,
        ...foldKeymap,
        ...completionKeymap,
        ...lintKeymap
      ]),
      // defaultHighlightStyle,
      cursorTooltipField.extension,
      markdownLanguageSupport,
      // EditorView.updateListener.of((v) => {
      //   // console.log(v.transactions)
      //   // if (v.docChanged) {
      //   //   console.log('change', v.changes);
      //   // }
      // }),
      EditorView.domEventHandlers({
        click: (event, editorView) => {
          startCompletion(editorView);
        }
      }),
      theme,
      checkboxPlugin.extension,
      markdownDecorator,
      urlDecorator,
      inlineImagePlugin,
      autocompletion({
        selectOnOpen: true,
        activateOnTyping: true,
        aboveCursor: true,
        closeOnBlur: true,
        override: [async (context: CompletionContext): Promise<CompletionResult | null> => {
          const firstToken = context.matchBefore(/[^ ]*/).text[0]

          if (firstToken != '/') {
            return null;
          }

          const selection = context.state.wordAt(context.pos);
          function resolveQuery() {
            if (selection) {
              return context.state.sliceDoc(selection.from, selection.to)
            } else {
              return '';
            }
          }

          return {
            from: (selection?.from || context.pos)-1,
            filter: false,
            options: this.autoSuggestionsProvider(resolveQuery())
          };
        }
        ]
      })
      // EditorView.inputHandler.of((view, from, to, text) => {
      //   if (text === ' ') {
      //     closeCompletion(view);
      //   } else {
      //     startCompletion(view);
      //   }
      //   return false
      // })
    ];
    return extensions;
  }

  setFocus() {
    this.editorView.focus();
  }

  private setText(text: string) {
    console.log('setText', text);
    if (this.editorView) {
      this.editorView.destroy();
    }

    const state = EditorState.create({doc: text, extensions: this.getExtensions()});
    this.editorView = new EditorView({
      state,
      parent: this.editor.nativeElement,
    });
    this.setFocus();
  }

  getText() {
    return this.editorView.state.doc.toString()
  }
}
