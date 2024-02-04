import {LanguageDescription, LanguageSupport} from "@codemirror/language";
import {InlineContext, DelimiterType, Line} from "@lezer/markdown";
import {markdown} from "@codemirror/lang-markdown";

const HashtagDelim = { resolve: "hashtag", mark: "hashtag" };

export const markdownLanguageSupport = markdown({
  extensions: [
    {defineNodes: [
        { name: 'hashtag'}
      ],
      parseInline: [
        {
          name: 'hashtag',
          parse(cx: InlineContext, next: number, pos: number): number {
            // console.log('InlineContext', next, cx.char(pos))
            if (cx.char(pos) === '#'.charCodeAt(0) && cx.char(pos + 1) !== 32) {

              let end = pos+1;
              while(end < cx.end-1 && ![0, 32].includes(cx.char(end))) {
                end++;
              }
              return cx.addDelimiter(HashtagDelim, pos, end, true, false)
            }

            return -1;
          }
        }
      ]
    }
  ],
  codeLanguages: [
    LanguageDescription.of({
      name: "javascript",
      alias: ["js", "jsx"],
      async load() {
        const { javascriptLanguage } = await import(
          "@codemirror/lang-javascript"
          );
        return new LanguageSupport(javascriptLanguage);
      }
    }),
    LanguageDescription.of({
      name: "css",
      async load() {
        const { cssLanguage } = await import("@codemirror/lang-css");
        return new LanguageSupport(cssLanguage);
      }
    }),
    LanguageDescription.of({
      name: "json",
      async load() {
        const { jsonLanguage } = await import("@codemirror/lang-json");
        return new LanguageSupport(jsonLanguage);
      }
    }),
    LanguageDescription.of({
      name: "html",
      alias: ["htm"],
      async load() {
        const { jsxLanguage } = await import("@codemirror/lang-javascript");
        const javascript = new LanguageSupport(jsxLanguage);
        const { cssLanguage } = await import("@codemirror/lang-css");
        const css = new LanguageSupport(cssLanguage);
        const { htmlLanguage } = await import("@codemirror/lang-html");

        return new LanguageSupport(htmlLanguage, [css, javascript]);
      }
    })
  ]
});
