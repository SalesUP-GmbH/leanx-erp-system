"use client"

import withAuth from '@/utils/withAuth';
import React, { useState, useEffect, Suspense } from 'react'
import { 
  User, 
  Building2, 
  Award, 
  BookOpen, 
  Briefcase, 
  GraduationCap,
  Plus,
  X,
  Save
} from "lucide-react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import DashboardLayout from "@/components/dashboard/dashboard-layout"

export default withAuth(function ProfilePage() {
  const [certificates, setCertificates] = useState([
    { name: 'AWS Certified Solutions Architect', year: '2023', issuer: 'Amazon Web Services' },
    { name: 'Certified Scrum Master', year: '2022', issuer: 'Scrum Alliance' }
  ]);

  const [projects, setProjects] = useState([
    {
      name: 'ERP System Implementation',
      company: 'Tech Corp GmbH',
      duration: '8 months',
      description: 'Led the implementation of a company-wide ERP system'
    }
  ]);

  const [profileData, setProfileData] = useState<any>(null);

  useEffect(() => {
    const fetchEmployeeProfile = async () => {
      try {
        const response = await fetch('/api/employee/self');
        const data = await response.json();
        setProfileData(data);
      } catch (error) {
        console.error('Error fetching profile:', error);
      }
    };
    fetchEmployeeProfile();
  }, []);


  return (
    <Suspense fallback={<div>Loading...</div>}>
      <DashboardLayout>
        <div className="flex-1 space-y-8 p-4 md:p-8 pt-6">
          {/* Header */}
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-3xl font-bold tracking-tight">Profil</h2>
              <p className="text-muted-foreground">Verwalten Sie Ihre persönlichen und beruflichen Informationen</p>
            </div>
            <Button>
              <Save className="mr-2 h-4 w-4" />
              Änderung anfragen
            </Button>
          </div>

          <div className="grid gap-8">
            {/* Personal Information */}
            <Card className="border-2 border-black">
              <CardHeader>
                <div className="flex items-center space-x-4">
                  <Avatar className="h-20 w-20">
                    <AvatarImage src="/placeholder.svg" />
                    <AvatarFallback>
                      {profileData?.firstName?.[0] || '?'}{profileData?.lastName?.[0] || ''}
                    </AvatarFallback>
                  </Avatar>
                  <div>
                    <CardTitle>Persönliche Informationen</CardTitle>
                    <CardDescription>Ihre grundlegenden Informationen</CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {profileData ? (
                  <div className="grid gap-4 md:grid-cols-2">
                    <div>
                      <label className="text-sm font-medium">Name</label>
                      <Input value={decodeURIComponent(profileData?.firstName || '') + ' ' + decodeURIComponent(profileData?.lastName || '')} readOnly />
                    </div>
                    <div>
                      <label className="text-sm font-medium">Email</label>
                      <Input value={profileData?.email} readOnly />
                    </div>
                    <div>
                      <label className="text-sm font-medium">Beschäftigungsart</label>
                      <Input
                        value={
                          profileData?.employmentType === 'FULL_TIME'
                            ? 'Vollzeit'
                            : profileData?.employmentType === 'PART_TIME'
                            ? 'Teilzeit'
                            : profileData?.employmentType === 'INTERN'
                            ? 'Praktikant'
                            : profileData?.employmentType || ''
                        }
                        readOnly
                      />
                    </div>
                    <div>
                      <label className="text-sm font-medium">Status</label>
                      <Input
                        value={
                          profileData?.employmentStatus === 'ACTIVE'
                            ? 'Aktiv'
                            : profileData?.employmentStatus === 'TERMINATED'
                            ? 'Beendet'
                            : profileData?.employmentStatus === 'RESIGNED'
                            ? 'Gekündigt'
                            : profileData?.employmentStatus === 'RETIRED'
                            ? 'In Rente'
                            : profileData?.employmentStatus === 'ON_LEAVE'
                            ? 'Beurlaubt'
                            : profileData?.employmentStatus === 'SUSPENDED'
                            ? 'Suspendiert'
                            : profileData?.employmentStatus || ''
                        }
                        readOnly
                      />
                    </div>
                    <div>
                      <label className="text-sm font-medium">Startdatum</label>
                      <Input value={profileData?.startDate ? new Date(profileData.startDate).toLocaleDateString() : ''} readOnly />
                    </div>
                  </div>
                ) : (
                  <div>Lade Profildaten...</div>
                )}
              </CardContent>
            </Card>

            {/* Professional Information */}
            <Card className="border-2 border-black">
              <CardHeader>
                <CardTitle>Berufliche Informationen</CardTitle>
                <CardDescription>Ihre Position und Expertise</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid gap-4 md:grid-cols-2">
                  <div>
                    <label className="text-sm font-medium">Position</label>
                    <Input value={profileData?.jobTitle || ''} readOnly />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Bereich</label>
                    <Input value={profileData?.department || ''} readOnly />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Manager</label>
                    <Input value={decodeURIComponent(profileData?.managerFirstName || '') + ' ' + decodeURIComponent(profileData?.managerLastName || '')} readOnly />
                  </div>
                </div>
                <div>
                  <label className="text-sm font-medium">Fachgebiete</label>
                  <Textarea 
                    defaultValue=""
                    className="mt-1"
                  />
                </div>
              </CardContent>
            </Card>

            {/* Certificates */}
            <Card className="border-2 border-black">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>Zertifizierungen</CardTitle>
                    <CardDescription>Ihre erworbenen Zertifikate</CardDescription>
                  </div>
                  <Button variant="outline">
                    <Plus className="h-4 w-4 mr-2" />
                    Zertifikat hinzufügen
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {certificates.map((cert, index) => (
                    <div key={index} className="flex items-center justify-between p-4 border rounded-lg">
                      <div className="flex items-center space-x-4">
                        <Award className="h-8 w-8 text-blue-500" />
                        <div>
                          <p className="font-medium">{cert.name}</p>
                          <p className="text-sm text-muted-foreground">
                            {cert.issuer} • {cert.year}
                          </p>
                        </div>
                      </div>
                      <Button variant="ghost" size="icon">
                        <X className="h-4 w-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Project References */}
            <Card className="border-2 border-black">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>Projektreferenzen</CardTitle>
                    <CardDescription>Ihre durchgeführten Projekte</CardDescription>
                  </div>
                  <Button variant="outline">
                    <Plus className="h-4 w-4 mr-2" />
                    Projekt hinzufügen
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {projects.map((project, index) => (
                    <div key={index} className="p-4 border rounded-lg">
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center space-x-4">
                          <Briefcase className="h-6 w-6 text-blue-500" />
                          <div>
                            <p className="font-medium">{project.name}</p>
                            <p className="text-sm text-muted-foreground">
                              {project.company} • {project.duration}
                            </p>
                          </div>
                        </div>
                        <Button variant="ghost" size="icon">
                          <X className="h-4 w-4" />
                        </Button>
                      </div>
                      <p className="text-sm text-muted-foreground ml-10">
                        {project.description}
                      </p>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Skills and Expertise */}
            <Card className="border-2 border-black">
              <CardHeader>
                <CardTitle>Skills & Expertise</CardTitle>
                <CardDescription>Ihre Fähigkeiten und Kenntnisse</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div>
                    <label className="text-sm font-medium">Technische Skills</label>
                    <Textarea 
                      defaultValue="SAP ERP, Oracle Database, Salesforce, JIRA, Microsoft Azure"
                      className="mt-1"
                    />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Methodenkompetenz</label>
                    <Textarea 
                      defaultValue="Scrum, Kanban, Six Sigma, ITIL, Prince2"
                      className="mt-1"
                    />
                  </div>
                  <div>
                    <label className="text-sm font-medium">Sprachen</label>
                    <Textarea 
                      defaultValue="Deutsch (Muttersprache), Englisch (C1), Französisch (B2)"
                      className="mt-1"
                    />
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </DashboardLayout>
    </Suspense>
  )
})