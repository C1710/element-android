/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.matrix.android.internal.session.sync

import im.vector.matrix.android.internal.database.model.ReadReceiptEntity
import io.realm.Realm


// the receipts dictionnaries
// key   : $EventId
// value : dict key $UserId
//              value dict key ts
//                    dict value ts value
typealias ReadReceiptContent = Map<String, Map<String, Map<String, Map<String, Double>>>>

internal class ReadReceiptHandler {

    fun handle(realm: Realm, roomId: String, content: ReadReceiptContent?): List<ReadReceiptEntity> {
        if (content == null) {
            return emptyList()
        }
        val readReceipts = content
                .flatMap { (eventId, receiptDict) ->
                    receiptDict
                            .filterKeys { it == "m.read" }
                            .flatMap { (_, userIdsDict) ->
                                userIdsDict.map { (userId, paramsDict) ->
                                    val ts = paramsDict.filterKeys { it == "ts" }
                                            .values
                                            .firstOrNull() ?: 0.0
                                    val primaryKey = roomId + userId
                                    ReadReceiptEntity(primaryKey, userId, eventId, roomId, ts)
                                }
                            }
                }
        realm.insertOrUpdate(readReceipts)
        return readReceipts
    }

}