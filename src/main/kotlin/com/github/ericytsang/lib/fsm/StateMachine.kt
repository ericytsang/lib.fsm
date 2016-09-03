package com.github.ericytsang.lib.fsm

import com.github.ericytsang.lib.observe.BackedField
import java.util.concurrent.locks.ReentrantLock

class StateMachine<State:StateMachine.BaseState>(initialState:State):BackedField<State>(initialState)
{
    init
    {
        initialState.onEnter()
    }

    val stateAccess = ReentrantLock()

    override fun FieldAccess<State>.setter(proposedValue:State)
    {
        if (!stateAccess.isHeldByCurrentThread)
        {
            throw IllegalStateException("thread must hold lock to stateAccess before setting state")
        }

        val oldValue = field
        field.onExit()
        if (field !== oldValue)
        {
            throw IllegalStateException("implementation of onExit must not change the state of the state machine")
        }

        field = proposedValue
        field.onEnter()
    }

    interface BaseState
    {
        fun onEnter()
        fun onExit()
    }
}
