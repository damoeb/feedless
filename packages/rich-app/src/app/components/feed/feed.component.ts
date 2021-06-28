import {Component, Input, OnInit} from '@angular/core';
import {ModalController} from "@ionic/angular";
import {DiscoveredFeed} from "../../../generated/graphql";
import {Apollo, gql} from "apollo-angular";

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.scss'],
})
export class FeedComponent implements OnInit {

  @Input()
  feed: DiscoveredFeed

  constructor(private readonly modalController: ModalController,
              private readonly apollo: Apollo) { }

  ngOnInit() {}

  getItems() {
    return this.apollo.watchQuery<any>({
      query: gql`query {
        findFirstUser(where: { email: {equals: "karl@may.ch"} }) {
          id
          email
          name
          feeds {
            title
            feedType
          }
          buckets {
            id
            title
            subscriptions {
              ownerId
              feed {
                url
                title
              }
            }
          }
        }
      }
      `
    });
  }


  dismissModal() {
    return this.modalController.dismiss();
  }}
