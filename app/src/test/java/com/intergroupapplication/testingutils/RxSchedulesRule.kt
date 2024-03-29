package com.intergroupapplication.testingutils

import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


/**
 * Created by abakarmagomedov on 22/08/2018 at project InterGroupApplication.
 */
class RxSchedulesRule(private val worker: Scheduler = Schedulers.trampoline()) : TestRule {

    private val immediate = Schedulers.from(Runnable::run)

    private val testScheduler: TestScheduler by lazy {
        return@lazy TestScheduler()
    }

    override fun apply(base: Statement?, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                RxJavaPlugins.reset()
                RxAndroidPlugins.reset()

                setSchedulers(worker, immediate)

                base?.evaluate()

                RxJavaPlugins.reset()
                RxAndroidPlugins.reset()
            }

        }
    }

    fun initWithTestScheduler(): TestScheduler {
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }
        return testScheduler
    }

    private fun setSchedulers(workerScheduler: Scheduler, mainThreadScheduler: Scheduler) {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { mainThreadScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { workerScheduler }
        RxJavaPlugins.setIoSchedulerHandler { workerScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { workerScheduler }
    }

}
