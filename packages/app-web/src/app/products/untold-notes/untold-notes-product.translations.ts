import { NotebookActionId, UntoldLanguages } from '../../services/notebook.service';

interface UntoldNotesProductTranslations {
  actions: {
    [K in keyof typeof NotebookActionId]: string;
  };
  defaultTemplates: {
    label: string;
    templateValue: string;
  }[];
}

export const translations: Record<UntoldLanguages, UntoldNotesProductTranslations> = {
  de: {
    actions: {
      deleteNote: 'Notiz löschen',
      cloneNote: 'Notiz duplizieren',
      followupNote: 'Folgenotiz erstellen',
      moveNote: 'Notiz verschieben',
      pinNote: 'Notiz anheften',
      attachFile: 'Datei anhängen',
    },
    defaultTemplates: [
      {
        label: 'Flüchtige Notiz',
        templateValue: `# Flüchtige Notiz – {{date}}

- `,
      },
      {
        label: 'Konzept Notiz',
        templateValue: `# {{title}}

ID: {{noteId}}
Parent: {{parentId}}
Pinned: false
Tags:

---`,
      },
      {
        label: 'Struktur Notiz',
        templateValue: `# Thema: [Übergeordnetes Thema]

## Unterthemen / Cluster:
- [[Begriff 1]]
- [[Begriff 2]]
- [[Zusammenhang A↔B]]
- [[Theorie XY]]

## Kommentar:
Wichtige Fragen, Spannungen, offene Probleme, Forschungsstand etc.

## Weiterführende Zettel:
- [[Diskussion XYZ]]
- [[Anwendung in Kontext ABC]]

## Tags:
#Struktur #Themencluster #[Thema]`,
      },
      {
        label: 'Verbindungsnotiz',
        templateValue: `# Zusammenhang: [Thema A] ↔ [Thema B]

## Zusammenhang:
Kurze Erklärung, wie die beiden Ideen sich beeinflussen, ergänzen oder widersprechen.

## Verlinkte Zettel:
- [[Thema A]]
- [[Thema B]]

## Kommentar / Bedeutung:
Warum ist diese Verbindung wichtig? Was ergibt sich daraus?

## Tags:
#Verbindung #Kontrast #Integration`,
      },
      {
        label: 'Begriffsnotiz',
        templateValue: `# Begriff: [Begriffstitel]

## Definition:
Kurze, klare Definition – in eigenen Worten.

## Quelle (optional):
Autor, Werk, Jahr, Seite.

## Kontext / Anwendung:
Wo spielt der Begriff eine Rolle? Wofür ist er wichtig?
`,
      },
      {
        label: 'Fakt Notiz',
        templateValue: `# Titel der Idee (prägnant, aussagekräftig)

## Quelle:
Autor, Titel, Jahr, Seitenzahl (z. B. Kant, Kritik der reinen Vernunft, 1781, A51/B75)

## Inhalt:
Kurze, präzise Zusammenfassung in eigenen Worten. (KEINE Copy-Paste!)

## Zitat (optional):
"Originalzitat bei Bedarf" – Kant (1781), A51

## Kommentar (optional):
Eigene Einschätzung, Kritik, offene Fragen.

## Tags:`,
      },
    ],
  },
  en: {
    actions: {
      deleteNote: 'Delete Note',
      cloneNote: 'Clone Note',
      followupNote: 'Followup Note',
      moveNote: 'Move Note',
      pinNote: 'Pin Note',
      attachFile: 'Attach File',
    },
    defaultTemplates: [
      {
        label: 'fleeting',
        templateValue: `# Fleeting Note – {{date}}

- `,
      },
      {
        label: 'concept',
        templateValue: `# {{title}}

ID: {{noteId}}
Parent: {{parentId}}
Pinned: false
Tags:

---`,
      },
      {
        label: 'structure',
        templateValue: `# Topic: [Parent Topic]

## Subtopics / Clusters:
- [[Term 1]]
- [[Term 2]]
- [[Connection A↔B]]
- [[Theory XY]]

## Comment:
Key questions, tensions, open problems, state of research, etc.

## Related Notes:
- [[Discussion XYZ]]
- [[Application in Context ABC]]

## Tags:
#Structure #TopicCluster #[Topic]`,
      },
      {
        label: 'connection',
        templateValue: `# Connection: [Topic A] ↔ [Topic B]

## Connection:
Brief explanation of how the two ideas influence, complement, or contradict each other.

## Linked Notes:
- [[Topic A]]
- [[Topic B]]

## Comment / Significance:
Why is this connection important? What follows from it?

## Tags:
#Connection #Contrast #Integration`,
      },
      {
        label: 'definition',
        templateValue: `# Term: [Term Title]

## Definition:
Brief, clear definition – in your own words.

## Source (optional):
Author, work, year, page.

## Context / Application:
Where does the term play a role? Why is it important?
`,
      },
      {
        label: 'fact',
        templateValue: `# Title of the Idea (concise, meaningful)

## Source:
Author, title, year, page number (e.g., Kant, Critique of Pure Reason, 1781, A51/B75)

## Content:
Brief, precise summary in your own words. (NO copy-paste!)

## Quote (optional):
"Original quote if needed" – Kant (1781), A51

## Comment (optional):
Your own evaluation, critique, open questions.

## Tags:`,
      },
    ],
  },
};
