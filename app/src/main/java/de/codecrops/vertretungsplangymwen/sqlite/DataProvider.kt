package de.codecrops.vertretungsplangymwen.sqlite

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log

class DataProvider :  ContentProvider() {

    private lateinit var mDBHelper : DataBaseHelper

    private val LEHRER = 100
    private val LEHRER_ID = 101
    private val VERTRETUNGSPLAN = 102
    private val VERTRETUNGSPLAN_ID = 103

    //Name, welcher bei Fehlermeldungen mit ausgegeben wird
    private val LOG_TAG = this.javaClass.simpleName

    private val sUriMatcher : UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        sUriMatcher.addURI(DBContracts.CONTENT_AUTHORITY, DBContracts.PATH_LEHRER, LEHRER)
        sUriMatcher.addURI(DBContracts.CONTENT_AUTHORITY, DBContracts.PATH_LEHRER + "/#", LEHRER_ID)
        sUriMatcher.addURI(DBContracts.CONTENT_AUTHORITY, DBContracts.PATH_SCHEDULE, VERTRETUNGSPLAN)
        sUriMatcher.addURI(DBContracts.CONTENT_AUTHORITY, DBContracts.PATH_SCHEDULE + "/#", VERTRETUNGSPLAN_ID)

    }

    override fun onCreate(): Boolean {
        mDBHelper = DataBaseHelper(context)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {

        val db = mDBHelper.readableDatabase

        val cursor : Cursor

        //Figure out if the UriMatcher can match the URI to a specific Code
        val match = sUriMatcher.match(uri)

        when(match) {
            LEHRER -> cursor = db.query(DBContracts.LehrerContract.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            LEHRER_ID -> {
                val localselection = DBContracts.LehrerContract._ID + "=?"
                // HIER HAB ICH DAS EINFACH WEGGELASSEN, da es nicht funktioniert hat :D Sollte auch ohne gehen: selectionArgs = Array<String>(ContentUris.parseId(uri).toInt()) { ContentUris.parseId(uri).toString()}
                cursor = db.query(DBContracts.LehrerContract.TABLE_NAME, projection, localselection, selectionArgs, null, null, sortOrder)
            }
            VERTRETUNGSPLAN -> cursor = db.query(DBContracts.PlanContract.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            VERTRETUNGSPLAN_ID -> {
                val localselection = DBContracts.PlanContract._ID + "=?"
                // HIER HAB ICH DAS EINFACH WEGGELASSEN, da es nicht funktioniert hat :D Sollte auch ohne gehen: selectionArgs = Array<String>(ContentUris.parseId(uri).toInt()) { ContentUris.parseId(uri).toString()}
                cursor = db.query(DBContracts.PlanContract.TABLE_NAME, projection, localselection, selectionArgs, null, null, sortOrder)
            }
            else -> throw IllegalArgumentException("Cannot query unknown URI $uri!")
        }

        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues): Uri? {
        val match = sUriMatcher.match(uri)
        when(match) {
            LEHRER -> return insertLehrer(uri, values)
            VERTRETUNGSPLAN -> return insertVertretungsplan(uri, values)
            else -> throw IllegalArgumentException("Insertion is not supported for $uri")
        }
    }

    private fun insertLehrer(uri: Uri, values: ContentValues) : Uri? {
        //Sanity-Check
        var invalidData = false
        //Lehrer-Kuerzel-Validation
        val kuerzelValue = values.getAsString(values.getAsString(DBContracts.LehrerContract.COLUMN_KUERZEL))
        if(kuerzelValue.isNullOrBlank() || kuerzelValue.length != 3) {
            invalidData = true
        }
        //Nachname-Kuerzel-Validation
        val nachnameValue = values.getAsString(values.getAsString(DBContracts.LehrerContract.COLUMN_NACHNAME))
        if(nachnameValue.isNullOrBlank()) {
            invalidData = true
        }
        //invalidData-Check
        if(invalidData) {
            throw java.lang.IllegalArgumentException("Failed to insert new Lehrer into DB! Invalid Values!")
        }

        val db = mDBHelper.writableDatabase
        val id : Long = db.insert(DBContracts.LehrerContract.TABLE_NAME, null, values)

        //db.insert() fehlgeschlagen:
        if(id.equals(-1)) {
            Log.e(LOG_TAG, "Failed to insert row for $uri")
            return null
        }

        //db.insert() hat funktioniert
        return ContentUris.withAppendedId(uri, id)
    }

    private fun insertVertretungsplan(uri: Uri, values: ContentValues) : Uri? {
        //Sanity-Check
        var invalidData = false
        //Klasse-Validation
        val klasseValue = values.getAsString(values.getAsString(DBContracts.PlanContract.COLUMN_KLASSE))
        if(klasseValue.isNullOrBlank()) {
            invalidData = true
        }
        //Stunde-Validation
        val stundeValue = values.getAsInteger(values.getAsString(DBContracts.PlanContract.COLUMN_STUNDE))
        if(stundeValue == null || stundeValue <= 0 || stundeValue > 10 ) {
            invalidData = true
        }
        //Vertretung-Validation
        val vertretungValue = values.getAsString(values.getAsString(DBContracts.PlanContract.COLUMN_VERTRETUNG))
        if(vertretungValue.isNullOrBlank() || vertretungValue.length != 3) {
            invalidData = true
        }
        //invalidData-Check
        if(invalidData) {
            throw java.lang.IllegalArgumentException("Failed to insert new Vertretungsplan (Stunde) into DB! Invalid Values!")
        }

        val db = mDBHelper.writableDatabase
        val id : Long = db.insert(DBContracts.PlanContract.TABLE_NAME, null, values)

        //db.insert() fehlgeschlagen:
        if(id.equals(-1)) {
            Log.e(LOG_TAG, "Failed to insert row for $uri")
            return null
        }

        //db.insert() hat funktioniert
        return ContentUris.withAppendedId(uri, id)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(uri: Uri, values: ContentValues, selection: String?, selectionArgs: Array<String>?): Int {
        val match = sUriMatcher.match(uri)
        when(match) {
            LEHRER -> return updateLehrer(uri, values, selection, selectionArgs)
            LEHRER_ID -> {
                val localselection = DBContracts.LehrerContract._ID + "=?"
                return updateLehrer(uri, values, localselection, selectionArgs)
            }
            VERTRETUNGSPLAN -> return updateVertretungsplan(uri, values, selection, selectionArgs)
            VERTRETUNGSPLAN_ID -> {
                val localselection = DBContracts.LehrerContract._ID + "=?"
                return updateVertretungsplan(uri, values, localselection, selectionArgs)
            }
            else -> throw java.lang.IllegalArgumentException("Update is not supported for $uri")
        }
    }

    private fun updateLehrer(uri: Uri, values: ContentValues, selection: String?, selectionArgs: Array<String>?) : Int {
        //Sanity-Check
        //Size-Check (Überprüfung, ob values überhaupt Daten enthält. Sorgt für bessere Performance)
        if(values.size() == 0) return 0
        //Kürzel-Check (Muss genau 3 Zeichen lang sein)
        if(values.getAsString(DBContracts.LehrerContract.COLUMN_KUERZEL).length != 3) {
            System.err.print("[DataProvider] Lehrer-Kürzel muss genau 3 Zeichen lang sein!")
            return 0
        }
        //Nachname-Check (Darf nicht leer oder null sein)
        if(values.getAsString(DBContracts.LehrerContract.COLUMN_NACHNAME).isNullOrBlank()) {
            System.err.print("[DataProvider] Lehrer-Nachname darf nicht leer sein!")
            return 0
        }
        //DB-Update
        return mDBHelper.writableDatabase.update(DBContracts.LehrerContract.TABLE_NAME, values, selection, selectionArgs)
    }

    private fun updateVertretungsplan(uri: Uri, values: ContentValues, selection: String?, selectionArgs: Array<String>?) : Int {
        //Sanity-Check
        //Size-Check (Überprüfung, ob values überhaupt Daten enthält. Sorgt für bessere Performance)
        if(values.size() == 0) return 0
        //Klasse-Check entfällt, da es auch leere "Klassen" gibt
        //Stunde-Check (Darf nicht 0 oder null sein)
        if(values.getAsInteger(DBContracts.PlanContract.COLUMN_STUNDE).equals(0)) {
            System.err.print("[DataProvider] Plan-Stunde darf nicht 0 sein!")
            return 0
        }
        //Vertretung-Check (Darf nicht leer oder null sein)
        if(values.getAsString(DBContracts.PlanContract.COLUMN_VERTRETUNG).isNullOrBlank()) {
            System.err.print("[DataProvider] Plan-Vertretung darf nicht leer oder null sein!")
            return 0
        }
        //DB-Update
        return mDBHelper.writableDatabase.update(DBContracts.PlanContract.TABLE_NAME, values, selection, selectionArgs)
    }

    override fun getType(uri: Uri): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}