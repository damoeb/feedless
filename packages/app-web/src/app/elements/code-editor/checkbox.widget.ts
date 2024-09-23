import {
  Decoration,
  DecorationSet,
  EditorView,
  PluginValue,
  ViewPlugin,
  ViewUpdate,
  WidgetType,
} from '@codemirror/view';
import { syntaxTree } from '@codemirror/language';

class CheckboxWidget extends WidgetType {
  constructor(readonly checked: boolean) {
    super();
  }

  override eq(other: CheckboxWidget) {
    return other.checked == this.checked;
  }

  override toDOM() {
    let wrap = document.createElement('span');
    wrap.setAttribute('aria-hidden', 'true');
    wrap.className = 'cm-boolean-toggle';
    let box = wrap.appendChild(document.createElement('input'));
    box.type = 'checkbox';
    box.checked = this.checked;
    return wrap;
  }

  override ignoreEvent() {
    return false;
  }
}

function checkboxes(view: EditorView) {
  let widgets: any[] = [];
  for (let { from, to } of view.visibleRanges) {
    syntaxTree(view.state).iterate({
      from,
      to,
      enter: (node) => {
        if (node.name == 'BooleanLiteral') {
          let isTrue = view.state.doc.sliceString(node.from, node.to) == 'true';
          let deco = Decoration.widget({
            widget: new CheckboxWidget(isTrue),
            side: 1,
          });
          widgets.push(deco.range(node.to));
        }
      },
    });
  }
  return Decoration.set(widgets);
}

function toggleBoolean(view: EditorView, pos: number) {
  let before = view.state.doc.sliceString(Math.max(0, pos - 5), pos);
  let change;
  if (before == 'false') {
    change = { from: pos - 5, to: pos, insert: 'true' };
  } else if (before.endsWith('true')) {
    change = { from: pos - 4, to: pos, insert: 'false' };
  } else {
    return false;
  }
  view.dispatch({ changes: change });
  return true;
}

export const checkboxPlugin = ViewPlugin.fromClass(
  class implements PluginValue {
    decorations: DecorationSet;

    constructor(view: EditorView) {
      this.decorations = checkboxes(view);
    }

    update(update: ViewUpdate) {
      if (update.docChanged || update.viewportChanged) {
        this.decorations = checkboxes(update.view);
      }
    }
  },
  {
    decorations: (v) => v.decorations,

    eventHandlers: {
      mousedown: (e, view) => {
        let target = e.target as HTMLElement;
        if (
          target.nodeName == 'INPUT' &&
          target.parentElement!.classList.contains('cm-boolean-toggle')
        ) {
          return toggleBoolean(view, view.posAtDOM(target));
        }
        return false;
      },
    },
  },
);
