package com.github.ericytsang.lib.fsm

import com.github.ericytsang.lib.observe.Change
import org.junit.Test
import kotlin.concurrent.withLock

/**
 * Created by surpl on 8/18/2016.
 */
class StateMachineTest
{
    @Test
    fun waterToDeath()
    {
        try
        {
            val plant = Plant()
            plant.water()
            plant.water()
            plant.water()
            plant.watch()
            plant.watch()
            plant.water()
            plant.watch()
            plant.water()
            plant.water()
            plant.watch()
            plant.water()
            plant.water()
            plant.water()
            plant.water()
            plant.water()
            plant.water()
            assert(false,{"plant somehow did not die"})
        }
        catch (ex:IllegalStateException)
        {
            ex.printStackTrace()
            println("excellent, the plant is dead...")
        }
    }

    @Test
    fun takeCareOfPlant()
    {
        try
        {
            val plant = Plant()
            plant.watch()
            plant.watch()
            plant.water()
            plant.water()
            plant.watch()
        }
        catch (ex:IllegalStateException)
        {
            ex.printStackTrace()
            assert(false,{"oh no, plant died!"})
        }
    }

    @Test
    fun watchToDeath()
    {
        try
        {
            val plant = Plant()
            plant.watch()
            plant.watch()
            plant.watch()
            plant.water()
            plant.watch()
            plant.watch()
            plant.watch()
            plant.watch()
            assert(false,{"plant somehow did not die"})
        }
        catch (ex:IllegalStateException)
        {
            ex.printStackTrace()
            println("excellent, the plant is dead...")
        }
    }

    @Test
    fun changeStateWithoutLock()
    {
        try
        {
            val plant = Plant()
            plant.changeStateWithoutLock()
            assert(false,{"we didnt get an exception..."})
        }
        catch (ex:IllegalStateException)
        {
            ex.printStackTrace()
            println("excellent, we had an exception...")
        }
    }

    class Plant
    {
        private val stateMachine = StateMachine<State>(Alive())

        init
        {
            stateMachine.observers += Change.Observer.new()
            {
                stateMachine.stateAccess
            }
        }

        fun water() = stateMachine.value.water()
        fun watch() = stateMachine.value.watch()
        fun changeStateWithoutLock() = stateMachine.value.changeStateWithoutLock()

        private interface State:StateMachine.BaseState
        {
            fun water()
            fun watch()
            fun changeStateWithoutLock()
        }

        private inner class Alive:State
        {
            private val MAX_LIFE = 3

            private var life = MAX_LIFE

            override fun onEnter()
            {
                println("Alive")
            }

            override fun onExit()
            {
                // do nothing
            }

            override fun water() = stateMachine.stateAccess.withLock()
            {
                if (++life > MAX_LIFE)
                {
                    stateMachine.value = Dead()
                }
                println("life: $life")
            }

            override fun watch() = stateMachine.stateAccess.withLock()
            {
                if (--life < 0)
                {
                    stateMachine.value = Dead()
                }
                println("life: $life")
            }

            override fun changeStateWithoutLock()
            {
                stateMachine.value = Dead()
            }
        }

        private inner class Dead:State
        {
            override fun onEnter()
            {
                println("Dead")
            }

            override fun onExit()
            {
                throw IllegalStateException("final state")
            }

            override fun water()
            {
                throw IllegalStateException("already dead")
            }

            override fun watch()
            {
                throw IllegalStateException("already dead")
            }

            override fun changeStateWithoutLock()
            {
                stateMachine.value = Alive()
            }
        }
    }
}
