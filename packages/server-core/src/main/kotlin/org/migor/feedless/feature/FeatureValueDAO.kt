package org.migor.feedless.feature

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.features} & ${AppLayer.repository}")
interface FeatureValueDAO : JpaRepository<FeatureValueEntity, UUID> {

  fun findByFeatureGroupIdAndFeatureId(planId: UUID, featureId: UUID): FeatureValueEntity?

  @Query(
    value = """
     WITH RECURSIVE plan_tree(id, parent_plan_id, depth) AS (
    SELECT t.id, t.parent_feature_group_id, 0
    FROM t_feature_group t
    where id = :featureGroupId
    UNION ALL
    SELECT t.id, t.parent_feature_group_id, depth + 1
    FROM t_feature_group t,
         plan_tree st
    WHERE t.id = st.parent_plan_id)
SELECT distinct on (f.name) fv.id, fv.value_int, fv.value_bool, fv.value_type, fv.feature_group_id, fv.feature_id, fv.created_at
FROM plan_tree pl
         inner join t_feature_value fv on pl.id = fv.feature_group_id
         inner join t_feature f on f.id = fv.feature_id
where f.name = :feature
order by f.name, depth

  """,
    nativeQuery = true,
  )
  fun resolveByFeatureGroupIdAndName(
    @Param("featureGroupId") featureGroupId: UUID,
    @Param("feature") feature: String
  ): FeatureValueEntity?


  @Query(
    value = """
     WITH RECURSIVE plan_tree(id, parent_plan_id, depth) AS (
    SELECT t.id, t.parent_feature_group_id, 0
    FROM t_feature_group t
    where id = :featureGroupId
    UNION ALL
    SELECT t.id, t.parent_feature_group_id, depth + 1
    FROM t_feature_group t,
         plan_tree st
    WHERE t.id = st.parent_plan_id)
SELECT distinct on (f.name) fv.id, fv.value_int, fv.value_bool, fv.value_type, fv.feature_group_id, fv.feature_id, fv.created_at
FROM plan_tree pl
         inner join t_feature_value fv on pl.id = fv.feature_group_id
         inner join t_feature f on f.id = fv.feature_id
order by f.name, depth

  """,
    nativeQuery = true,
  )
  fun resolveAllByFeatureGroupId(@Param("featureGroupId") featureGroupId: UUID): List<FeatureValueEntity>
  fun findAllByFeatureGroupId(featureGroupId: UUID): List<FeatureValueEntity>
  fun deleteAllByFeatureGroupId(id: UUID)

}
