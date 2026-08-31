{{/*
Expand the name of the chart.
*/}}
{{- define "ecommerce.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "ecommerce.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "ecommerce.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "ecommerce.labels" -}}
helm.sh/chart: {{ include "ecommerce.chart" . }}
{{ include "ecommerce.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "ecommerce.selectorLabels" -}}
app.kubernetes.io/name: {{ include "ecommerce.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the namespace to use
*/}}
{{- define "ecommerce.namespace" -}}
{{- .Values.namespace | default "ecommerce" }}
{{- end }}

{{/*
Kafka bootstrap servers string
Note: Kafka uses kafka-{0,1,2}.kafka-svc.<namespace>.svc.cluster.local
*/}}
{{- define "ecommerce.kafkaBootstrapServers" -}}
{{- $replicas := int (toString (.Values.kafka.replicas | default 3)) }}
{{- $namespace := include "ecommerce.namespace" . }}
{{- $port := int (toString (.Values.kafka.port | default 9092)) }}
{{- $servers := list }}
{{- range $i := until $replicas }}
{{- $servers = append $servers (printf "kafka-%d.kafka-svc.%s.svc.cluster.local:%d" $i $namespace $port) }}
{{- end }}
{{- join "," $servers }}
{{- end }}
