package com.example.viakaab.data

import com.example.viakaab.model.EatsCategory
import com.example.viakaab.model.LocalCommerce
import com.example.viakaab.model.StoreProduct
import com.example.viakaab.ui.theme.BrandBlue
import com.example.viakaab.ui.theme.BrandEmerald
import com.example.viakaab.ui.theme.BrandSky

object CarrilloCommerceCatalog {

    val commerces: List<LocalCommerce> = listOf(
        // ---------------- CAFETERÍAS, FONDAS Y ANTOJITOS ----------------
        LocalCommerce(
            id = "store_cafeteria_kiosco",
            name = "Cafetería El Kiosco Maya",
            category = EatsCategory.ANTOJITOS,
            neighborhood = "Centro · Frente al Parque Principal",
            deliveryTimeLabel = "12-20 min",
            deliveryFeeMxn = 25,
            rating = "4.9",
            lat = 19.5776,
            lng = -88.0452,
            accentColor = BrandEmerald,
            subtitle = "Café de olla, frappés, club sándwich y repostería casera",
            products = listOf(
                StoreProduct(
                    id = "p_frappe_horchata",
                    name = "Frappé de Horchata con Espresso",
                    description = "Bebida fría de la casa con canela y crema batida.",
                    priceMxn = 68,
                    badge = "Favorito"
                ),
                StoreProduct(
                    id = "p_cafe_olla",
                    name = "Café de Olla Artesanal 16 oz",
                    description = "Con piloncillo y canela recién preparado.",
                    priceMxn = 42
                ),
                StoreProduct(
                    id = "p_club_sandwich",
                    name = "Club Sándwich de Pollo + Papas",
                    description = "Pan artesanal tostado, jamón, queso, pollo y tocino.",
                    priceMxn = 95
                ),
                StoreProduct(
                    id = "p_pay_queso_bola",
                    name = "Rebanada de Pay de Queso de Bola",
                    description = "Receta yucateca suave con topping de queso edam.",
                    priceMxn = 60
                )
            )
        ),
        LocalCommerce(
            id = "store_fonda_mestiza",
            name = "Fonda La Mestiza FCP",
            category = EatsCategory.ANTOJITOS,
            neighborhood = "Col. Centro · Calle 65",
            deliveryTimeLabel = "15-25 min",
            deliveryFeeMxn = 25,
            rating = "4.9",
            lat = 19.5768,
            lng = -88.0442,
            accentColor = BrandEmerald,
            subtitle = "Comida corrida, poc chuc, caldo de pavo y agua de chaya",
            products = listOf(
                StoreProduct(
                    id = "p_comida_poc_chuc",
                    name = "Comida Corrida: Poc Chuc al Carbón",
                    description = "Incluye arroz, frijol colado, tortillas hechas a mano y chiltomate.",
                    priceMxn = 105,
                    badge = "Menú del día"
                ),
                StoreProduct(
                    id = "p_caldo_pavo",
                    name = "Caldo de Pavo Regional (1 L)",
                    description = "Con verduras, arroz, limón, cilantro y chile dulce.",
                    priceMxn = 98
                ),
                StoreProduct(
                    id = "p_agua_chaya_1l",
                    name = "Agua Fresca de Chaya con Piña 1 L",
                    description = "Refrescante bebida tradicional quintanarroense.",
                    priceMxn = 38
                )
            )
        ),
        LocalCommerce(
            id = "store_cruz_parlante",
            name = "Lonchería La Cruz Parlante",
            category = EatsCategory.ANTOJITOS,
            neighborhood = "Centro · Calle 67 x 66",
            deliveryTimeLabel = "15-25 min",
            deliveryFeeMxn = 25,
            rating = "4.9",
            lat = 19.5778,
            lng = -88.0450,
            accentColor = BrandEmerald,
            subtitle = "Panuchos, salbutes, cochinita pibil y aguas frescas",
            products = listOf(
                StoreProduct(
                    id = "p_panuchos_3",
                    name = "Orden de 3 Panuchos de Cochinita",
                    description = "Con cebolla morada curtida, aguacate y chile habanero aparte.",
                    priceMxn = 65,
                    badge = "Popular"
                ),
                StoreProduct(
                    id = "p_salbutes_3",
                    name = "Orden de 3 Salbutes de Pollo",
                    description = "Recién fritos con pollo deshebrado, repollo, tomate y aguacate.",
                    priceMxn = 60
                ),
                StoreProduct(
                    id = "p_torta_cochinita",
                    name = "Torta Especial de Cochinita Pibil",
                    description = "Bolillo calientito con cochinita horneada, frijol y xnipec.",
                    priceMxn = 55
                ),
                StoreProduct(
                    id = "p_agua_horchata",
                    name = "Agua Fresca de Horchata 1 L",
                    description = "Preparada al día con canela y bien fría.",
                    priceMxn = 35
                )
            )
        ),
        LocalCommerce(
            id = "store_crucero_maya",
            name = "Taquería El Crucero Maya",
            category = EatsCategory.ANTOJITOS,
            neighborhood = "Av. Santiago Pacheco Cruz",
            deliveryTimeLabel = "15-20 min",
            deliveryFeeMxn = 25,
            rating = "4.8",
            lat = 19.5792,
            lng = -88.0438,
            accentColor = BrandEmerald,
            subtitle = "Tacos al pastor, asada, gringas y papas preparadas",
            products = listOf(
                StoreProduct(
                    id = "p_tacos_pastor_5",
                    name = "Orden de 5 Tacos al Pastor",
                    description = "Tortilla taquera, piña asada, cilantro, cebolla y salsas.",
                    priceMxn = 85,
                    badge = "Más pedido"
                ),
                StoreProduct(
                    id = "p_gringa_pastor",
                    name = "Gringa Grande de Pastor",
                    description = "Tortilla de harina dorada con queso Oaxaca fundido y carne al pastor.",
                    priceMxn = 75
                ),
                StoreProduct(
                    id = "p_torta_asada",
                    name = "Torta de Carne Asada",
                    description = "Con guacamole, frijoles, queso y pico de gallo.",
                    priceMxn = 70
                ),
                StoreProduct(
                    id = "p_refresco_600",
                    name = "Refresco Embotellado 600 ml",
                    description = "Bien frío (Coca-Cola, Sprite o Manzanita).",
                    priceMxn = 25
                )
            )
        ),
        LocalCommerce(
            id = "store_asador_nohoch",
            name = "Asadero Nohoch Pollo",
            category = EatsCategory.ANTOJITOS,
            neighborhood = "Col. Jesús Martínez Ross",
            deliveryTimeLabel = "20-30 min",
            deliveryFeeMxn = 25,
            rating = "4.8",
            lat = 19.5812,
            lng = -88.0415,
            accentColor = BrandEmerald,
            subtitle = "Pollo asado al carbón, arroz, tortillas y salsas caseras",
            products = listOf(
                StoreProduct(
                    id = "p_pollo_entero",
                    name = "Pollo Asado Entero Familiar",
                    description = "Incluye arroz rojo, tortillas a mano, cebollitas asadas y 2 salsas.",
                    priceMxn = 195,
                    badge = "Familiar"
                ),
                StoreProduct(
                    id = "p_medio_pollo",
                    name = "Medio Pollo Asado",
                    description = "Acompañado de arroz, ensalada fresca, tortillas y salsa.",
                    priceMxn = 110
                ),
                StoreProduct(
                    id = "p_frijol_charro",
                    name = "Orden de Frijoles Charros 1/2 L",
                    description = "Con tocino, salchicha y chorizo.",
                    priceMxn = 40
                )
            )
        ),
        LocalCommerce(
            id = "store_dona_lupe",
            name = "Antojitos Doña Lupe",
            category = EatsCategory.ANTOJITOS,
            neighborhood = "Mercado Municipal Benito Juárez",
            deliveryTimeLabel = "15-25 min",
            deliveryFeeMxn = 25,
            rating = "4.9",
            lat = 19.5761,
            lng = -88.0431,
            accentColor = BrandEmerald,
            subtitle = "Empanadas de queso de bola, kibis y relleno negro",
            products = listOf(
                StoreProduct(
                    id = "p_empanadas_4",
                    name = "4 Empanadas de Queso de Bola",
                    description = "Crujientes y doraditas con salsa de tomate y repollo.",
                    priceMxn = 75,
                    badge = "Favorito FCP"
                ),
                StoreProduct(
                    id = "p_kibis_3",
                    name = "Orden de 3 Kibis Preparados",
                    description = "Con cebolla morada encurtida y salsa habanera.",
                    priceMxn = 55
                ),
                StoreProduct(
                    id = "p_torta_relleno",
                    name = "Torta de Relleno Negro",
                    description = "Pavo en recado negro tradicional con huevo cocido.",
                    priceMxn = 65
                )
            )
        ),

        // ---------------- FARMACIAS ----------------
        LocalCommerce(
            id = "store_farmacia_yza",
            name = "Farmacia Yza Carrillo",
            category = EatsCategory.FARMACIA,
            neighborhood = "Av. Benito Juárez · Centro",
            deliveryTimeLabel = "10-20 min",
            deliveryFeeMxn = 25,
            rating = "4.9",
            lat = 19.5772,
            lng = -88.0446,
            accentColor = BrandBlue,
            subtitle = "Medicamentos básicos, sueros rehidratantes y cuidado diario",
            products = listOf(
                StoreProduct(
                    id = "f_paracetamol",
                    name = "Paracetamol 500 mg (Caja 20 tabs)",
                    description = "Alivio rápido de fiebre y dolor leve o moderado.",
                    priceMxn = 45,
                    badge = "Esencial"
                ),
                StoreProduct(
                    id = "f_electrolit",
                    name = "Suero Oral Electrolit 625 ml",
                    description = "Rehidratación rápida (fresa, coco, uva o manzana).",
                    priceMxn = 32
                ),
                StoreProduct(
                    id = "f_ibuprofeno",
                    name = "Ibuprofeno 400 mg (Caja 10 tabs)",
                    description = "Analgésico y antiinflamatorio de uso común.",
                    priceMxn = 58
                ),
                StoreProduct(
                    id = "f_antigripal",
                    name = "Antigripal Día y Noche (12 caps)",
                    description = "Alivia congestión nasal, cuerpo cortado y fiebre.",
                    priceMxn = 95
                ),
                StoreProduct(
                    id = "f_curitas_botiquin",
                    name = "Kit Curitas + Alcohol + Algodón",
                    description = "Paquete de primeros auxilios listo para el hogar.",
                    priceMxn = 78
                )
            )
        ),
        LocalCommerce(
            id = "store_similares_fcp",
            name = "Farmacias Similares FCP",
            category = EatsCategory.FARMACIA,
            neighborhood = "Av. Lázaro Cárdenas",
            deliveryTimeLabel = "10-20 min",
            deliveryFeeMxn = 25,
            rating = "4.8",
            lat = 19.5784,
            lng = -88.0462,
            accentColor = BrandBlue,
            subtitle = "Genéricos económicos, vitaminas, jarabes y botiquín",
            products = listOf(
                StoreProduct(
                    id = "f_vitamina_c",
                    name = "Vitamina C Efervescente (10 tabs)",
                    description = "Refuerzo diario para el sistema inmune sabor naranja.",
                    priceMxn = 65
                ),
                StoreProduct(
                    id = "f_jarabe_tos",
                    name = "Jarabe Ambroxol Adulto 120 ml",
                    description = "Expectorante para aliviar flemas y tos.",
                    priceMxn = 55
                ),
                StoreProduct(
                    id = "f_omeprazol",
                    name = "Omeprazol 20 mg (14 cápsulas)",
                    description = "Protector gástrico contra acidez y agruras.",
                    priceMxn = 42,
                    badge = "Oferta"
                ),
                StoreProduct(
                    id = "f_gel_repelente",
                    name = "Repelente de Mosquitos 120 ml",
                    description = "Protección efectiva contra mosquitos de la región.",
                    priceMxn = 89
                )
            )
        ),
        LocalCommerce(
            id = "store_del_ahorro_fcp",
            name = "Farmacia del Ahorro Centro",
            category = EatsCategory.FARMACIA,
            neighborhood = "Frente al Parque Principal",
            deliveryTimeLabel = "15-25 min",
            deliveryFeeMxn = 25,
            rating = "4.9",
            lat = 19.5774,
            lng = -88.0457,
            accentColor = BrandBlue,
            subtitle = "Cuidado del bebé, dermocosmética, higiene y farmacia",
            products = listOf(
                StoreProduct(
                    id = "f_panales",
                    name = "Pañales Etapa 4 (Paquete 20 pzas)",
                    description = "Absorción nocturna suave para bebé.",
                    priceMxn = 165
                ),
                StoreProduct(
                    id = "f_bloqueador",
                    name = "Bloqueador Solar FPS 50+",
                    description = "Resistente al agua y sudor para el sol de Quintana Roo.",
                    priceMxn = 210
                ),
                StoreProduct(
                    id = "f_termometro",
                    name = "Termómetro Digital Clínico",
                    description = "Lectura rápida con pantalla digital.",
                    priceMxn = 115
                )
            )
        ),

        // ---------------- SÚPER Y DESPENSA ----------------
        LocalCommerce(
            id = "store_chedraui_fcp",
            name = "Chedraui Felipe Carrillo Puerto",
            category = EatsCategory.SUPER,
            neighborhood = "Carr. Mérida - Chetumal",
            deliveryTimeLabel = "25-35 min",
            deliveryFeeMxn = 30,
            rating = "4.8",
            lat = 19.5825,
            lng = -88.0418,
            accentColor = BrandSky,
            subtitle = "Despensa completa, lácteos, carnes, abarrotes y hogar",
            products = listOf(
                StoreProduct(
                    id = "s_reja_huevo",
                    name = "Reja de Huevo Blanco (30 pzas)",
                    description = "Huevo fresco de primera calidad.",
                    priceMxn = 98,
                    badge = "Básico"
                ),
                StoreProduct(
                    id = "s_leche_entera",
                    name = "Pack 2 Leche Entera 1 L",
                    description = "Leche ultrapasteurizada adicionada con vitaminas.",
                    priceMxn = 58
                ),
                StoreProduct(
                    id = "s_garrafon_20l",
                    name = "Garrafón de Agua Purificada 20 L",
                    description = "Agua purificada lista para el hogar (cambio o relleno).",
                    priceMxn = 48
                ),
                StoreProduct(
                    id = "s_papel_higienico",
                    name = "Papel Higiénico (Paquete 12 rollos)",
                    description = "Doble hoja extra suave.",
                    priceMxn = 95
                ),
                StoreProduct(
                    id = "s_pechuga_pollo",
                    name = "Pechuga de Pollo Fresca 1 kg",
                    description = "Empacada el mismo día para tu cocina.",
                    priceMxn = 145
                )
            )
        ),
        LocalCommerce(
            id = "store_super_aki_fcp",
            name = "Súper Akí Carrillo",
            category = EatsCategory.SUPER,
            neighborhood = "Av. Constituyentes",
            deliveryTimeLabel = "15-25 min",
            deliveryFeeMxn = 25,
            rating = "4.8",
            lat = 19.5750,
            lng = -88.0441,
            accentColor = BrandSky,
            subtitle = "Abarrotes, granos, tortillas, bebidas y limpieza",
            products = listOf(
                StoreProduct(
                    id = "s_kit_despensa",
                    name = "Combo Despensa FCP (Arroz + Frijol + Aceite)",
                    description = "1 kg arroz, 1 kg frijol negro y 1 L aceite vegetal.",
                    priceMxn = 128,
                    badge = "Ahorro"
                ),
                StoreProduct(
                    id = "s_tortillas_1kg",
                    name = "Tortillas de Maíz Calientes 1 kg",
                    description = "Envueltas en papel grado alimenticio.",
                    priceMxn = 26
                ),
                StoreProduct(
                    id = "s_detergente_1kg",
                    name = "Detergente en Polvo 1 kg",
                    description = "Para ropa blanca y de color.",
                    priceMxn = 45
                ),
                StoreProduct(
                    id = "s_coca_25l",
                    name = "Refresco Familiar 2.5 L",
                    description = "Ideal para la comida en familia.",
                    priceMxn = 42
                )
            )
        ),
        LocalCommerce(
            id = "store_oxxo_centro",
            name = "OXXO Parque Principal",
            category = EatsCategory.SUPER,
            neighborhood = "Centro · Av. Benito Juárez",
            deliveryTimeLabel = "10-15 min",
            deliveryFeeMxn = 25,
            rating = "4.9",
            lat = 19.5777,
            lng = -88.0454,
            accentColor = BrandSky,
            subtitle = "Botanas, café Andatti, hielo, bebidas y antojos 24h",
            products = listOf(
                StoreProduct(
                    id = "s_bolsa_hielo",
                    name = "Bolsa de Hielo 5 kg",
                    description = "Hielo purificado para reuniones y bebidas.",
                    priceMxn = 38
                ),
                StoreProduct(
                    id = "s_cafe_andatti",
                    name = "Café Andatti Grande + Pan Dulce",
                    description = "Americano o capuchino recién hecho con pieza de pan.",
                    priceMxn = 52,
                    badge = "Express"
                ),
                StoreProduct(
                    id = "s_combo_botana",
                    name = "Papas Grandes + 2 Refrescos 600 ml",
                    description = "Combo listo para ver el partido o película.",
                    priceMxn = 92
                )
            )
        ),
        LocalCommerce(
            id = "store_fruteria_mercado",
            name = "Frutería y Verduras El Mercado",
            category = EatsCategory.SUPER,
            neighborhood = "Mercado Municipal FCP",
            deliveryTimeLabel = "15-25 min",
            deliveryFeeMxn = 25,
            rating = "4.9",
            lat = 19.5760,
            lng = -88.0430,
            accentColor = BrandSky,
            subtitle = "Frutas frescas, verdura del día, limón, aguacate y queso",
            products = listOf(
                StoreProduct(
                    id = "s_canasta_verduras",
                    name = "Canasta Fresca (Tomate, Cebolla, Limón y Chile)",
                    description = "1 kg tomate saladet, 1 kg cebolla blanca, 1 kg limón y chiles.",
                    priceMxn = 105,
                    badge = "Del día"
                ),
                StoreProduct(
                    id = "s_aguacate_1kg",
                    name = "Aguacate Hass Maduro 1 kg",
                    description = "Seleccionados listos para comer.",
                    priceMxn = 82
                ),
                StoreProduct(
                    id = "s_queso_fresco",
                    name = "Queso Blanco Regional 500 g",
                    description = "Queso fresco de rancho ideal para antojitos.",
                    priceMxn = 75
                )
            )
        )
    )

    fun byCategory(category: EatsCategory): List<LocalCommerce> =
        if (category == EatsCategory.ALL) commerces
        else commerces.filter { it.category == category }

    fun findById(id: String): LocalCommerce? =
        commerces.firstOrNull { it.id == id }
}
