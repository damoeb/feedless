// import { Injectable } from '@angular/core';
// import { cloneDeep, groupBy, isNull, isUndefined } from 'lodash-es';
// import { Note } from './notebook.service';
// import { notebookRepository } from './notebook-repository';
//
// export type NotificationLevel = 'off' | 'info' | 'warn'
//
// export interface UtRepoSettings {
//   // dateFormat: string;
//   // timeFormat: string;
//   // editorFontFamily: string;
//   // embedPdf: boolean,
//   minClusterSize?: number,
//   smallCluster: NotificationLevel;
//   noDeadLinks: NotificationLevel,
//   noDuplicateTitles: NotificationLevel;
//   noEmptyNotes: NotificationLevel,
//   checkRepositoryHealth: boolean,
//   // archiveSnapshotImages: boolean,
//   // ocr: boolean,
//   // template: string,
//   // themeURL?: string,
// }
//
// export function isOff(level: NotificationLevel) {
//   return !level || level == 'off';
// }
//
// export function justLines(text: string, longerThan = 0): string[] {
//   return text.split(/\n/)
//     .filter(line => line.trim().length > longerThan)
// }
//
// @Injectable()
// export class NotebookHealthService {
//   private repoSettings: UtRepoSettings = {
//     minClusterSize: 3,
//     smallCluster: 'info',
//     noDeadLinks: 'info',
//     noDuplicateTitles: 'info',
//     noEmptyNotes: 'info',
//     checkRepositoryHealth: true,
//   };
//
//   getRepoSettings(): UtRepoSettings {
//     return cloneDeep(this.repoSettings);
//   }
//
//   private async checkRepositoryHealth() {
//     const settings = this.getRepoSettings();
//     console.log('checkRepositoryHealth', settings.checkRepositoryHealth);
//     if (settings.checkRepositoryHealth) {
//       const notesWithRefs = await notebookRepository.notes.()
//         .reduce<Promise<Note[]>>((waitFor, note) =>
//           waitFor
//             .then(async (agg) => {
//               agg.push({
//                 note,
//                 refs: await this.findReferences(note, {outgoing: true, incoming: true})
//               })
//               return agg;
//             }), Promise.resolve([] as Note[]));
//
//       // duplicate names
//       if (!isOff(settings.noDuplicateTitles)) {
//         const groups = groupBy<Note>(this.store.getNotes(), 'title');
//         Object.keys(groups)
//           .filter(groupName => groups[groupName].length > 1)
//           .forEach((groupName) => this.notification.notifyDuplicateName(groups[groupName], settings.noDuplicateTitles));
//       } else {
//         console.log(`Won't validate noDeadLinks`)
//       }
//
//       // dead links
//       if (!isOff(settings.noDeadLinks)) {
//         notesWithRefs
//           .filter(nr => nr.refs.outgoing.some(outgoing => isNull(outgoing) || isUndefined(outgoing)))
//           .forEach((noteWithDeadlink) => this.notification.notifyDeadLink(noteWithDeadlink.note, settings.noDeadLinks));
//       } else {
//         console.log(`Won't validate noDeadLinks`)
//       }
//
//       // empty notes
//       if (!isOff(settings.noEmptyNotes)) {
//         this.store.getNotes()
//           .filter(note => !note.remoteContent)
//           .filter(note => justLines(note.content).length <= 1)
//           .forEach((emptyNote) => this.notification.notifyEmptyNote(emptyNote, settings.noEmptyNotes));
//       } else {
//         console.log(`Won't validate noDeadLinks`)
//       }
//
//       // if (settings.minClusterSize > 1 && !isOff(settings.smallCluster)) {
//       //   (await this.findSubGraphs().then(({rootGraph, otherGraphs}) => [rootGraph, ...otherGraphs]))
//       //     .filter(g => g.size <= settings.minClusterSize || g.size === 1)
//       //     .forEach(g => this.notification.notifySmallCluster(g, settings.smallCluster));
//       // } else {
//       //   console.log(`Won't validate minGraphSize`)
//       // }
//
//       this.events.trigger('noteChange');
//     } else {
//       console.log(`Won't validate checkRepositoryHealth`)
//     }
//   }
//
//
//   private async checkAbandonedNotes(note: Note) {
//     const abandonedNotes = await this.findReferences(note, {incoming: true});
//     if (abandonedNotes.incoming.length > 0) {
//       const alert = await this.alertController.create({
//         header: 'Broken Links',
//         message: `This note is linked in ${abandonedNotes.incoming.length} notes`,
//         buttons: [
//           {
//             text: 'Ignore',
//             role: 'cancel',
//           }, {
//             text: 'Show All',
//             handler: () => {
//               abandonedNotes.incoming.forEach(note => this.events.trigger('openTab', note.id))
//             }
//           }
//         ]
//       });
//       await alert.present();
//     }
//   }
//
// }
