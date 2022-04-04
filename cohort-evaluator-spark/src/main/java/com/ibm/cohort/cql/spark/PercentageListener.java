/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.spark;

import org.apache.spark.scheduler.SparkListener;
import org.apache.spark.scheduler.SparkListenerJobStart;
import org.apache.spark.scheduler.SparkListenerTaskEnd;
import org.apache.spark.scheduler.StageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PercentageListener extends SparkListener {

    private static final Logger LOG = LoggerFactory.getLogger(PercentageListener.class);

    private final List<String> stagePrefixes;
    private final Set<Integer> validStageIds = new HashSet<>();

    private long lastPercent = -1;
    private int totalTasks = 0;
    private int tasksCompleted = 0;

    public PercentageListener(List<String> stagePrefixes) {
        this.stagePrefixes = stagePrefixes;
    }

    @Override
    public void onTaskEnd(SparkListenerTaskEnd taskEnd) {
        if (taskEnd.taskInfo().failed()) {
            LOG.warn("Skipping task failure");
        }
        else if (validStageIds.contains(taskEnd.stageId())){
            tasksCompleted += 1;
            long currentPercent = Math.round((double) tasksCompleted / totalTasks * 100);
            if (currentPercent > lastPercent) {
                lastPercent = currentPercent;
                LOG.info("{}% Complete", currentPercent);
            }
        }
    }

    @Override
    public void onJobStart(SparkListenerJobStart event) {
        lastPercent = -1;
        validStageIds.clear();
        totalTasks = 0;
        tasksCompleted = 0;
        List<StageInfo> stageInfos = JavaConverters.seqAsJavaList(event.stageInfos());
        for (StageInfo stageInfo : stageInfos) {
            boolean stageMatch = stagePrefixes.stream()
                    .anyMatch(stageInfo.name()::startsWith);
            if (stageMatch) {
                validStageIds.add(stageInfo.stageId());
                totalTasks += stageInfo.numTasks();
            }
        }
        LOG.info("Tracking {} tasks in new job", totalTasks);
    }

}
