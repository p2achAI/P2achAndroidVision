package ai.p2ach.p2achandroidvision.base.repos

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/*
* 기본 RoomDataBase의 CRUD 작업을 수행할 수 있는 클래스, 상속받은 자식에서 기본적으로 Entitiy로만 데이터를 저장할 수 있음
* */

interface BaseDao<T> {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<T>)

    @Update
    suspend fun update(item: T)

    @Update
    suspend fun updateAll(items: List<T>)

    @Delete
    suspend fun delete(item: T)

    @Delete
    suspend fun deleteAll(items: List<T>)



}