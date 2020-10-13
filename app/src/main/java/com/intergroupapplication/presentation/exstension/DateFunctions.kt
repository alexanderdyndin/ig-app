package com.intergroupapplication.presentation.exstension

import com.github.marlonlom.utilities.timeago.TimeAgo
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by abakarmagomedov on 11/09/2018 at project InterGroupApplication.
 */

fun getDateDescribeByString(dateString: String): Single<String> {
    val postDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            .apply { timeZone = TimeZone.getTimeZone("GMT") }
    val postDate = postDateFormat.parse(dateString)
    return if (Locale.getDefault().displayLanguage == "русский") {
        getTrueTime().map {
            translateEnglishStringToRussian(TimeAgo.using(postDate.time, currentTime = it))
        }
    } else {
        getTrueTime().map { TimeAgo.using(postDate.time, currentTime = it) }
    }
}

fun getTrueTime(): Single<Long> {
    return Single.fromCallable {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.time.time
    }
}

fun translateEnglishStringToRussian(english: String): String {
    if (english.contains("within") || english.contains("tomorrow")) {
        return "только что"
    }

    if (english.contains("just now")) {
        return "только что"
    }

    if (english.contains("one minute ago")) {
        return "минуту назад"
    }

    if (english.contains("withing one minut")) {
        return "минуту назад"
    }

    if (english.contains("about an hour ago")) {
        return "час назад"
    }

    if (english.contains("yesterday")) {
        return "вчера"
    }

    if (english.contains("about a month ago")) {
        return "месяц назад"
    }

    if (english.contains("about a year ago")) {
        return "год назад"
    }

    if (english.contains("over a year ago")) {
        return "больше года назад"
    }

    if (english.contains("almost two years ago")) {
        return "два года назад"
    }

    if (english.contains("minutes ago")) {
        val minutes = english.substringBefore(" ").toInt()
        return when (minutes) {
            2, 3, 4 -> "$minutes минуты назад"
            in 5..20 -> "$minutes минут назад"
            21 -> "$minutes минуту назад"
            22, 23, 24 -> "$minutes минуты назад"
            in 25..30 -> "$minutes минут назад"
            31 -> "$minutes минуту назад"
            32, 33, 34 -> "$minutes минуты назад"
            in 35..40 -> "$minutes минут назад"
            41 -> "$minutes минуту назад"
            42, 43, 44 -> "$minutes минуты назад"
            in 45..50 -> "$minutes минут назад"
            51 -> "$minutes минуту назад"
            52, 53, 54 -> "$minutes минуты назад"
            in 55..60 -> "$minutes минут назад"
            else -> english
        }
    }


    if (english.contains("hours ago")) {
        val hours = english.substringBefore(" ").toInt()
        return when (hours) {
            2, 3, 4 -> "$hours  часа назад"
            in 5..20 -> "$hours часов назад"
            21 -> "$hours час назад"
            in 22..24 -> "$hours часа назад"
            else -> english
        }
    }

    if (english.contains("days ago")) {
        val days = english.substringBefore(" ").toInt()
        return when (days) {
            2, 3, 4 -> "$days дня назад"
            7 -> "неделю назад"
            14 -> "2 недели назад"
            in 5..20 -> "$days дней назад"
            21 -> "$days день назад"
            22, 23, 24 -> "$days дня назад"
            in 25..30 -> "$days дней назад"
            else -> english
        }
    }

    if (english.contains("months ago")) {
        val months = english.substringBefore(" ").toInt()
        return when (months) {
            2, 3, 4 -> "$months месяца назад"
            6 -> "полгода назад"
            in 5..12 -> "$months месяцев назад"
            else -> english
        }

    }

    if (english.contains("years ago")) {
        val years = english.substringBefore(" ").toInt()
        return when (years) {
            2, 3, 4 -> "$years года назад"
            in 5..12 -> "$years лет назад"
            else -> english
        }
    }
    return english
}

fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance()
    cal1.time = date1
    val cal2 = Calendar.getInstance()
    cal2.time = date2
    return isSameDay(cal1, cal2)
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
}
