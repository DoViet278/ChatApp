package com.example.chatapplication

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseManager {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://hvdsykpyexbptzgvefaa.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh2ZHN5a3B5ZXhicHR6Z3ZlZmFhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjEwMjk0NjAsImV4cCI6MjA3NjYwNTQ2MH0.kxDADlnhp5zkvETvMhHI2j7X-jDgVkZpTjBGiI55eD4"
    ){
        install(Storage)
    }
}