package org.migor.feedless.storage

import java.net.URI

interface GitConnectionConfig

data class AnonymousGitConnectionConfig(val urls: List<URI>) : GitConnectionConfig

data class AuthenticatedGitConnectionConfig(val credentials: GitAccountCredentials) : GitConnectionConfig
