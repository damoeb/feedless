import { TestBed } from '@angular/core/testing';

import { ReadabilityService } from './readability.service';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HTTP } from '@ionic-native/http/ngx';
import { TextToSpeech } from '@ionic-native/text-to-speech/ngx';

describe('ReadabilityService', () => {
  let service: ReadabilityService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ReadabilityService,
        { provide: HTTP, useValue: {} },
        { provide: TextToSpeech, useValue: {} },
      ],
      imports: [RouterTestingModule, HttpClientTestingModule],
    });
    service = TestBed.inject(ReadabilityService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
