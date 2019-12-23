package toplev.com.skyhookaccountability.support

import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import java.text.SimpleDateFormat
import java.util.*

internal object DateTimeApolloAdapter : CustomTypeAdapter<Date> {

    override fun encode(value: Date): CustomTypeValue<*> = CustomTypeValue.fromRawValue(value)
    override fun decode(value: CustomTypeValue<*>): Date {
        return try {
            val date = value.value as String
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(date)!!
        } catch (e: Exception) {
            e.printStackTrace()
            Date()
        }
    }
}