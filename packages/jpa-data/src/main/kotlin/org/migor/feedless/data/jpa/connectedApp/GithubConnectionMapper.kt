package org.migor.feedless.data.jpa.connectedApp

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.connectedApp.GithubConnection
import org.migor.feedless.data.jpa.IdMappers

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface GithubConnectionMapper {

    fun toDomain(entity: GithubConnectionEntity): GithubConnection
    fun toEntity(domain: GithubConnection): GithubConnectionEntity

    companion object {
        val INSTANCE: GithubConnectionMapper = Mappers.getMapper(GithubConnectionMapper::class.java)
    }
}
