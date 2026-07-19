package org.migor.feedless.document

import org.migor.feedless.guard.ResourceGuard

interface DocumentGuardPort : ResourceGuard<DocumentId, Document>
