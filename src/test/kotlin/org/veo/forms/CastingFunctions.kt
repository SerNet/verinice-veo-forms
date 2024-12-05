/**
 * verinice.veo forms
 * Copyright (C) 2022  Jonas Jordan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@file:Suppress("UNCHECKED_CAST")

package org.veo.forms

// Convenience functions for casting things in deserialized JSON response bodies.

fun Any?.asMap(): MutableMap<String, Any> = this as MutableMap<String, Any>

fun Any?.asNestedMap(): MutableMap<String, MutableMap<String, Any>> = this as MutableMap<String, MutableMap<String, Any>>

fun Any?.asListOfMaps(): MutableList<MutableMap<String, Any>> = this as MutableList<MutableMap<String, Any>>
