/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import android.content.res.Resources
import android.text.Spanned
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

        private var tonight = MutableLiveData<SleepNight?>()

        private var nights = database.getAllNight()

        val nightsString = Transformations.map(nights) { nights ->
                com.example.android.trackmysleepquality.formatNights(nights, application.resources)
        }

        init {
            initializeTonight()
        }

        private fun initializeTonight() {
                viewModelScope.launch {
                        tonight.value = getTonightFromDatabase()
                }

        }

        private suspend fun getTonightFromDatabase(): SleepNight? {
                var night = database.getToNight()
                // caso o horario de inicio seja diferente do fim significa que a night ja foi preenchida.
                if(night?.endTimeMilli != night?.startTimeMilli){
                        night = null
                }
                return night
        }

        fun onStartTracking(){
                viewModelScope.launch {
                        val newNight = SleepNight()
                        insert(newNight)
                        tonight.value = getTonightFromDatabase()
                }
        }

        private suspend fun insert(newNight: SleepNight) {
                database.insert(newNight)
        }

        fun onStopTracking(){
                viewModelScope.launch {
                        val oldNight = tonight.value ?: return@launch
                        oldNight.endTimeMilli = System.currentTimeMillis()
                        update(oldNight)
                }
        }

        private suspend fun update(oldNight: SleepNight){
                database.update(oldNight)
        }

        fun onClear(){
                viewModelScope.launch {
                        clear()
                        tonight.value = null
                }
        }

        private suspend fun clear(){
                database.clear()
        }

}

