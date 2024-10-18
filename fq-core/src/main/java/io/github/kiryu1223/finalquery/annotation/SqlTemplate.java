package io.github.kiryu1223.finalquery.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SqlTemplate
{
    String value();

    SqlTemplateType templateType() default SqlTemplateType.String;

    SqlType sqlType() default SqlType.Select;
}
