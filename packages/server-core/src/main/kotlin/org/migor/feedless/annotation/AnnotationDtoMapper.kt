package org.migor.feedless.annotation

import org.migor.feedless.generated.types.Annotation as AnnotationDto
import org.migor.feedless.generated.types.BoolAnnotation as BoolAnnotationDto
import org.migor.feedless.generated.types.TextAnnotation as TextAnnotationDto

fun Annotation.toDto(): AnnotationDto {
    return if (this is TextAnnotation) {
        AnnotationDto(
            id = id.uuid.toString(),
            text = TextAnnotationDto(
                fromChar = fromChar,
                toChar = toChar,
            )
        )
    } else {
        if (this is Vote) {
            val toBoolAnnotation = { value: Boolean ->
                BoolAnnotationDto(value)
            }

            AnnotationDto(
                id = id.uuid.toString(),
                flag = toBoolAnnotation(flag),
                upVote = toBoolAnnotation(upVote),
                downVote = toBoolAnnotation(downVote),
            )
        } else {
            throw IllegalArgumentException("Can't convert $this to annotation-dto")
        }
    }
}
