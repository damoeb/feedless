package org.migor.feedless.data.jpa.hostCooldown

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
@Profile("${AppProfiles.source} & ${AppLayer.repository}")
interface HostCooldownDAO : JpaRepository<HostCooldownEntity, String> {

  // Single statements, so concurrent workers on one host never lose an update.
  @Modifying(clearAutomatically = true)
  @Query(
    """
    INSERT INTO t_host_cooldown (host, blocked_until, strikes, last_status, updated_at)
    VALUES (:host, :until, 0, :status, :now)
    ON CONFLICT (host) DO UPDATE
      SET blocked_until = greatest(t_host_cooldown.blocked_until, excluded.blocked_until),
          last_status = excluded.last_status,
          updated_at = excluded.updated_at
  """, nativeQuery = true
  )
  fun upsertThrottled(
    @Param("host") host: String,
    @Param("until") until: LocalDateTime,
    @Param("status") status: Int,
    @Param("now") now: LocalDateTime,
  ): Int

  @Modifying(clearAutomatically = true)
  @Query(
    """
    INSERT INTO t_host_cooldown (host, blocked_until, strikes, last_status, updated_at)
    VALUES (:host, :now, 1, :status, :now)
    ON CONFLICT (host) DO UPDATE
      SET strikes = t_host_cooldown.strikes + 1,
          last_status = excluded.last_status,
          updated_at = excluded.updated_at
  """, nativeQuery = true
  )
  fun upsertStrike(
    @Param("host") host: String,
    @Param("status") status: Int,
    @Param("now") now: LocalDateTime,
  ): Int

  @Modifying(clearAutomatically = true)
  @Query(
    """
    UPDATE t_host_cooldown SET blocked_until = greatest(blocked_until, :until) WHERE host = :host
  """, nativeQuery = true
  )
  fun extendBlockedUntil(@Param("host") host: String, @Param("until") until: LocalDateTime): Int
}
