package com.example.proyectofinal.components.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyectofinal.helpers.RolHelper
import com.example.proyectofinal.repositorios.AutenticacionRepositorio
import com.example.proyectofinal.repositorios.ReporteRepositorio
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidacionEmergenciaScreen(
    navController: NavController,
    rolHelper: RolHelper,
    reporteId: String,
    onValidarComoPolicia: (String) -> Unit = {},
    onSolicitarValidacion: (String) -> Unit = {}
) {
    val authRepo = remember { AutenticacionRepositorio() }
    val reporteRepo = remember { ReporteRepositorio() }
    val scope = rememberCoroutineScope()

    var esPolicia by remember { mutableStateOf(false) }
    var esPropioReporte by remember { mutableStateOf(false) }
    var yaVoto by remember { mutableStateOf(false) }
    var categoria by remember { mutableStateOf("") }
    var estadoReporte by remember { mutableStateOf("en_revision") }
    var policiaHaVotado by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }
    var votando by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf<String?>(null) }
    var esError by remember { mutableStateOf(false) }

    LaunchedEffect(reporteId) {
        rolHelper.obtenerRol { rol -> esPolicia = rol == "policia" }
        val usuario = authRepo.obtenerDatosUsuarioActual()
        val reporte = reporteRepo.obtenerReportePorId(reporteId)

        if (usuario != null && reporte != null) {
            esPropioReporte = reporte.usuarioId == usuario.uid
            yaVoto = reporteRepo.yaVoto(reporteId, usuario.uid)
            categoria = reporte.categoria
            estadoReporte = reporte.estado
            policiaHaVotado = reporte.policiaHaVotado
        }
        cargando = false
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Verificar Incidente",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (cargando) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                return@Column
            }

            // Ícono
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (esPolicia) Icons.Default.GppGood else Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (categoria.isNotEmpty()) categoria.uppercase() else "INCIDENTE",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "Reporte #${reporteId.take(8)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF3B82F6)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                // 1. Resultado de una acción recién realizada
                mensajeResultado != null -> {
                    val cardColor = if (esError) Color(0xFFFFF1F2) else Color(0xFFEFF6FF)
                    val borderColor = if (esError) Color(0xFFFECACA) else Color(0xFFBFDBFE)
                    val iconColor = if (esError) Color(0xFFDC2626) else Color(0xFF2563EB)
                    val textColor = if (esError) Color(0xFF7F1D1D) else Color(0xFF1E3A8A)
                    val icon = if (esError) Icons.Default.Cancel else Icons.Default.CheckCircle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        border = BorderStroke(1.dp, borderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
                            Text(
                                mensajeResultado!!,
                                color = textColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    if (esError) {
                        OutlinedButton(
                            onClick = { mensajeResultado = null; esError = false },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) { Text("Reintentar", fontWeight = FontWeight.Bold) }
                    } else {
                        Button(
                            onClick = {
                                navController.popBackStack()
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) { Text("Volver a reportes", fontWeight = FontWeight.Bold) }
                    }
                }

                // 2. Reporte ya cerrado (verificado, falso, u otro estado final)
                estadoReporte !in listOf("en_revision", "activo") -> {
                    val esVerificado = estadoReporte == "verificado"
                    val colorFondo = if (esVerificado) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                    val colorBorde = if (esVerificado) Color(0xFFBBF7D0) else Color(0xFFFECACA)
                    val colorTexto = if (esVerificado) Color(0xFF166534) else Color(0xFF991B1B)
                    val titulo = if (esVerificado)
                        "Este reporte ya ha sido VERIFICADO."
                    else
                        "Este reporte ha sido marcado como FALSA ALARMA."
                    val icono = if (esVerificado) Icons.Default.CheckCircle else Icons.Default.Error

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colorFondo),
                        border = BorderStroke(1.dp, colorBorde),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(icono, null, tint = colorTexto, modifier = Modifier.size(32.dp))
                            Text(titulo, color = colorTexto, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text(
                                "Ya no es posible registrar más validaciones.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cerrar") }
                }

                // 3. Es el propio reporte del usuario
                esPropioReporte -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        border = BorderStroke(1.dp, Color(0xFFFFEBAA)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFF856404), modifier = Modifier.size(28.dp))
                            Text("No puedes validar tu propio reporte", color = Color(0xFF856404), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Volver") }
                }

                // 4. Ya votó antes
                yaVoto -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF64748B), modifier = Modifier.size(32.dp))
                            Text("Ya has participado en este reporte", fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            Text("Tu validación ya fue registrada previamente.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Volver") }
                }

                // 5. Ciudadano bloqueado: ya votó un policía
                !esPolicia && policiaHaVotado -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.GppGood, null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(32.dp))
                            Text("Reporte cerrado por autoridad", fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                            Text(
                                "Un oficial ya emitió su veredicto. No se aceptan más votos ciudadanos.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF3B82F6)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Volver") }
                }

                // 6. Panel de policía
                esPolicia -> {
                    Text(
                        "PANEL DE AUTORIDAD",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            votando = true
                            scope.launch {
                                val uid = authRepo.obtenerUsuarioActual()?.uid ?: ""
                                val res = reporteRepo.agregarVoto(reporteId, uid, voto = true, esPolicia = true)
                                votando = false
                                if (res.isSuccess) {
                                    onValidarComoPolicia(reporteId)
                                    esError = false
                                    mensajeResultado = "Reporte VERIFICADO correctamente."
                                } else {
                                    esError = true
                                    mensajeResultado = res.exceptionOrNull()?.message ?: "Error al verificar"
                                }
                            }
                        },
                        enabled = !votando,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (votando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        else {
                            Icon(Icons.Default.GppGood, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Confirmar Emergencia")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            votando = true
                            scope.launch {
                                val uid = authRepo.obtenerUsuarioActual()?.uid ?: ""
                                val res = reporteRepo.agregarVoto(reporteId, uid, voto = false, esPolicia = true)
                                votando = false
                                if (res.isSuccess) {
                                    esError = false
                                    mensajeResultado = "Reporte marcado como FALSA ALARMA."
                                } else {
                                    esError = true
                                    mensajeResultado = res.exceptionOrNull()?.message ?: "Error al marcar"
                                }
                            }
                        },
                        enabled = !votando,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = BorderStroke(1.5.dp, Color(0xFFDC2626))
                    ) {
                        Icon(Icons.Default.Block, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Marcar como Falsa Alarma")
                    }
                }

                // 7. Ciudadano: botones de votación
                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            "Tu validación ayuda a los servicios de emergencia a priorizar reportes reales. Se necesitan al menos 3 validaciones para confirmar un incidente.",
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1E3A8A),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            votando = true
                            esError = false
                            scope.launch {
                                val uid = authRepo.obtenerUsuarioActual()?.uid ?: ""
                                val resultado = reporteRepo.agregarVoto(reporteId, uid, voto = true, esPolicia = false)
                                votando = false
                                if (resultado.isSuccess) {
                                    esError = false
                                    mensajeResultado = "Validación registrada: marcado como real. ¡Gracias!"
                                } else {
                                    esError = true
                                    mensajeResultado = resultado.exceptionOrNull()?.message ?: "Error desconocido al registrar"
                                }
                            }
                        },
                        enabled = !votando,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (votando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        else {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Es real — Estoy siendo testigo", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            votando = true
                            esError = false
                            scope.launch {
                                val uid = authRepo.obtenerUsuarioActual()?.uid ?: ""
                                val resultado = reporteRepo.agregarVoto(reporteId, uid, voto = false, esPolicia = false)
                                votando = false
                                if (resultado.isSuccess) {
                                    esError = false
                                    mensajeResultado = "Validación registrada: marcado como falsa alarma."
                                } else {
                                    esError = true
                                    mensajeResultado = resultado.exceptionOrNull()?.message ?: "Error desconocido al registrar"
                                }
                            }
                        },
                        enabled = !votando,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE04F5F)),
                        border = BorderStroke(1.5.dp, Color(0xFFE04F5F))
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Falsa alarma / No ocurre", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
