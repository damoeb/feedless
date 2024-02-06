import { AfterViewInit, Component, ElementRef, ViewChild, ViewEncapsulation } from '@angular/core';

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
  private editorView: EditorView;

  constructor() {

  }

  ngAfterViewInit() {
    this.setText('');
  }

  private getExtensions() {
    const extensions: Extension[] = [
      gutter({
        renderEmptyElements: true
      }),
      lineNumbers(),
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
          const selection = context.state.wordAt(context.pos);
          if (!selection) {
            return null;
          }
          const word = context.state.sliceDoc(selection?.from, selection?.to);
          console.log('word', word);

          return {
            from: selection.from,
            filter: false,
            options: this.search(word)
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
    this.editorView.dom.focus();
  }

  private search(word: string) {
    return [];
  }

  setText(text: string) {
    console.log('setText', text);
    if (this.editorView) {
      this.editorView.destroy();
    }

    text = `

## Headings
# Heading level 1
## Heading level 2
### Heading level 3
#### Heading level 4


Heading level 1
===============

Heading level 2
---------------


## Hashtag

#stronly-typed

## Urls
https://example.org/foo/bar.html

## Code
At the command prompt, type \`nano\`.

## Code Block
\`\`\`
  println('this')
\`\`\`


## Horizontal Rules
***

---

_________________

## Bold
I just love **bold text**.
I just love __bold text__.
Love**is**bold

## Italic
Italicized text is the *cat's meow*.
Italicized text is the _cat's meow_.
A*cat*meow

## Blockquote
> Dorothy followed her through many of the beautiful rooms in her castle.

> Dorothy followed her through many of the beautiful rooms in her castle.
>
> The Witch bade her clean the pots and kettles and sweep the floor and keep the fire fed with wood.

> Dorothy followed her through many of the beautiful rooms in her castle.
>
>> The Witch bade her clean the pots and kettles and sweep the floor and keep the fire fed with wood.

## Lists
### Ordered Lists
1. First item
2. Second item
3. Third item
    1. Indented item
    2. Indented item
4. Fourth item

### Unordered Lists
- First item
- Second item
- Third item
- Fourth item

## Inline Image

![foo](https://mdg.imgix.net/assets/images/tux.png?auto=format&fit=clip&q=40&w=100)


    `

    const state = EditorState.create({doc: text, extensions: this.getExtensions()});
    this.editorView = new EditorView({
      state,
      parent: this.editor.nativeElement,
    });

  }
}
