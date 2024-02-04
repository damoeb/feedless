import {Decoration, DecorationSet, EditorView, ViewPlugin, ViewUpdate, WidgetType} from "@codemirror/view"
import {syntaxTree} from "@codemirror/language";
import {kebabCase} from "lodash";


function formatName(name: string) {
  return kebabCase(name);
}

function decorateMarkdown(view: EditorView) {
  const decorations: any[] = []

  for (let {from, to} of view.visibleRanges) {
    syntaxTree(view.state).iterate({
      from, to,
      enter: (node) => {
        // console.log(node.type.name, view.state.doc.sliceString(node.from, node.to))
        const deco = Decoration.mark({
          class: `cm-${formatName(node.name)}`
        })
        decorations.push(deco.range(node.from, node.to))
      }
    })
  }
  return Decoration.set(decorations)
}

export const markdownDecorator = ViewPlugin.fromClass(class {
  decorations: DecorationSet

  constructor(view: EditorView) {
    this.decorations = decorateMarkdown(view)
  }

  update(update: ViewUpdate) {
    if (update.docChanged || update.viewportChanged)
      this.decorations = decorateMarkdown(update.view)
  }
}, {
  decorations: v => v.decorations,
})
