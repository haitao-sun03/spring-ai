package com.haitao.ai.advisor;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;


public class ContextualQueryAugmenterFactory {

    public static QueryAugmenter create(boolean allowEmpty) {
        PromptTemplate emptyTemplate = PromptTemplate
                .builder()
                .template(
                        """
                                我只能回复面试相关问题，暂时无法回复您的问题，请联系客服处理
                                """
                )
                .build();

        return ContextualQueryAugmenter
                .builder()
                .allowEmptyContext(allowEmpty)
                .emptyContextPromptTemplate(emptyTemplate)
                .build();
    }

}
