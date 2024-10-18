package io.github.kiryu1223.finalquery.service;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.api.BasicJavacTask;

public class FinalPlugin implements Plugin
{
    @Override
    public String getName()
    {
        return "FinalQuery";
    }

    @Override
    public void init(JavacTask task, String... args)
    {
        BasicJavacTask javacTask = (BasicJavacTask) task;
        FinalTaskListener taskListener = new FinalTaskListener(javacTask.getContext());
        javacTask.addTaskListener(taskListener);
    }
}
