package com.dotcom.retail.config.db

import org.hibernate.boot.model.FunctionContributions
import org.hibernate.boot.model.FunctionContributor
import org.hibernate.type.StandardBasicTypes
import org.springframework.stereotype.Component

@Component
class FunctionContributor : FunctionContributor {

    override fun contributeFunctions(functionContributions: FunctionContributions) {
        val registry = functionContributions.functionRegistry
        val typeConfig = functionContributions.typeConfiguration
        val booleanType = typeConfig.basicTypeRegistry.resolve(StandardBasicTypes.BOOLEAN)
        val doubleType = typeConfig.basicTypeRegistry.resolve(StandardBasicTypes.DOUBLE)

        registry.registerPattern(
            "fts_match",
            "?1 @@ websearch_to_tsquery('simple', ?2)",
            booleanType
        )

        registry.registerPattern(
            "fts_rank",
            "ts_rank(?1, websearch_to_tsquery('simple', ?2))",
            doubleType
        )

        registry.registerPattern("word_similarity",
            "word_similarity(?1, ?2)",
            doubleType
        )
    }
}